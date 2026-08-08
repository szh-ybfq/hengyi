package com.zh.hengyi.admin.service.authority.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zh.hengyi.admin.mapper.authority.UserRoleMapper;
import com.zh.hengyi.admin.model.dto.authority.user.*;
import com.zh.hengyi.admin.model.entity.authority.UserRole;
import com.zh.hengyi.admin.model.vo.authority.user.UserFormVO;
import com.zh.hengyi.admin.model.vo.authority.user.UserLoginVO;
import com.zh.hengyi.admin.model.vo.authority.user.UserPageVO;
import com.zh.hengyi.admin.service.authority.UserService;
import com.zh.hengyi.common.enums.user.UserEnum;
import com.zh.hengyi.common.exception.BusinessException;
import com.zh.hengyi.common.result.ResultCode;
import com.zh.hengyi.config.sercurity.login.LoginUser;
import com.zh.hengyi.config.sercurity.utils.jwt.JwtUtil;
import com.zh.hengyi.admin.mapper.authority.UserMapper;
import com.zh.hengyi.admin.model.entity.authority.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static com.zh.hengyi.common.constant.AuthConstant.*;
import static com.zh.hengyi.common.result.ResultCode.ADMIN_NOT_DELETE;

@Service
@RequiredArgsConstructor // 相比@Autowired好处，见笔记
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    // 1、注册（新增插入，不用判断存在）
    /**
     * ⭐先查后插，易并发重复
     *      解决：判断存在校验+用户名唯一索引，索引名：uk_user_username
     * */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void register(UserRegisterDTO register) {
        // 1 判断用户名重名
        validUsernameUnique(register.getUsername());

        // 2 实体转换、密码加密
        User user = BeanUtil.copyProperties(register, User.class);
        user.setPassword(passwordEncoder.encode(register.getPassword()));

        // 3 插入
        userMapper.insert(user);
    }

    // 2、登录
    @Override
    public UserLoginVO login(UserLoginDTO login) {
        // 0 校验用户存在
        validUsernameExist(login.getUsername());

        // 1 调用认证
        Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);//校验用户密码
        } catch (LockedException e) {
            throw new BusinessException(ResultCode.USER_STATUS_LOCKED);//校验用户密码
        } catch (DisabledException e) {
            throw new BusinessException(ResultCode.USER_STATUS_FORBIDDEN);//校验用户密码
        } catch (AuthenticationException e) {
            // 所有其他认证异常父类兜底
            throw new BusinessException(ResultCode.USER_LOGIN_AUTH_FAIL, e.getMessage());
        }

        // 2 认证成功，拿到用户信息
        LoginUser loginUser = (LoginUser) auth.getPrincipal();
        // 3 认证成功，清除密码，后续上下文不再持有
        loginUser.getUser().setPassword(null);

        /*
        * 出现一直重复生成缓存、旧缓存没有被清除，同时改造login logout
        * 解决：
        *   双key方案
        *       用户索引存储token，token存储json对象，这样通过用户索引即可获取旧token，和新token比对，是否重复登录
        * */
        /*// 5.Redis 存储：LoginUser（服务端认证对象） 后期所有接口鉴权使用                         LoginUser转为JSON字符串存入Redis
        // objectMapper: JSON 序列化工具 Bean     writeValueAsString： 把 Java 对象 → JSON 字符串    opsForValue：操作 Redis String 类型数据，其中key:token, value:userJson
        String redisKey = "login:token:" + token;
        String userJson = objectMapper.writeValueAsString(loginUser);
        redisTemplate.opsForValue().set(redisKey, userJson, Duration.ofSeconds(expire));*/

        Long userId = loginUser.getUser().getId();
        loginUser.getUser().setPassword(null);

        // 2.构造key      userKey：用户索引的键key    oldToken：用户索引的值，又是会话的键的部分   oldTokenKey：旧会话的键
        String userKey = USER_PREFIX + userId + ":" + DEVICE;
        String oldToken = (String) redisTemplate.opsForValue().get(userKey);
        String oldTokenKey = TOKEN_PREFIX + oldToken;
        // 3.清理旧会话
        if (StrUtil.isNotBlank(oldToken)) {
            redisTemplate.delete(oldTokenKey);
            log.info("已清除用户登录会话缓存");
        }

        // 4.生成新token
        String newToken = jwtUtil.generateToken(userId);
        String newTokenKey = TOKEN_PREFIX + newToken; // oldTokenKey：新会话的键
        String userJson = objectMapper.writeValueAsString(loginUser); // 新会话的值

        // 5.存入两组缓存
        redisTemplate.opsForValue().set(userKey, newToken, Duration.ofSeconds(EXPIRE_SECOND));
        redisTemplate.opsForValue().set(newTokenKey, userJson, Duration.ofSeconds(EXPIRE_SECOND));
        log.info("用户新登录会话缓存成功");

        // 6 实体转换
        UserLoginVO vo = BeanUtil.copyProperties(loginUser.getUser(),UserLoginVO.class);
        vo.setToken(newToken);
//        vo.setRoleList();
//        vo.setPermissionList();
        return vo;
    }

    // 3、退出
    @Override
    public void logout(HttpServletRequest request) {
        // 校验：无token 或者 无值（json对象），过期返回
        String token = JwtUtil.extractToken(request);
        if (StrUtil.isBlank(token)) {
            return;
        }
        String curTokenKey = TOKEN_PREFIX + token;
        String userJson = (String) redisTemplate.opsForValue().get(curTokenKey);
        if (StrUtil.isBlank(userJson)) {
            return;
        }

        // 删除双key：用户索引、会话
        try {
            LoginUser loginUser = objectMapper.readValue(userJson, LoginUser.class); //json->java对象
            String userKey = USER_PREFIX + loginUser.getUser().getId() + ":" + DEVICE;

            redisTemplate.delete(userKey);
            redisTemplate.delete(curTokenKey);
            log.info("用户退出清除会话缓存成功");
        } catch (Exception e) {
            log.error("退出登录解析LoginUser失败", e);
        }
        // 清空Security上下文
        SecurityContextHolder.clearContext();
    }

    // 4、修改密码

    // 5、获取用户分页
    @Override
    public IPage<UserPageVO> getPage(UserQueryDTO dto) {
        IPage<User> userPage = userMapper.getPage(dto);
        return userPage.convert(user -> BeanUtil.copyProperties(user, UserPageVO.class));// convert返回IPage，里面total、pages、current、size全部自动拷贝
    }

    // 5.2 根据id获取用户
    @Override
    public UserFormVO getUserInfo(Long id) {
        User user = userMapper.selectById(id);
        return BeanUtil.copyProperties(user, UserFormVO.class);
    }

    // 6、添加用户
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(UserAddDTO dto) {
        validUsernameUnique(dto.getUsername());
        System.out.println("validUsernameUnique!!!!!!");
        User user = BeanUtil.copyProperties(dto,User.class);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        baseMapper.insert(user);
    }

    // 7、修改用户
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(UserEditDTO dto) {
        User old = baseMapper.selectById(dto.getId());
        if(old == null){
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        // 修改用户名做唯一校验，排除自己
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername,dto.getUsername());
        wrapper.ne(User::getId,dto.getId());
        Long count = baseMapper.selectCount(wrapper);
        if(count>0){
            throw new BusinessException(ResultCode.USERNAME_EXIST);
        }
        User user = BeanUtil.copyProperties(dto,User.class);
        baseMapper.updateById(user);
    }

    // 8、删除用户
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeByIdCheck(Long id) {
        // 不能删除超级管理员id=1
        if(id.equals(1L)){
            throw new BusinessException(ADMIN_NOT_DELETE);
        }
        baseMapper.deleteById(id);
        // 删除用户角色关联
        userRoleMapper.deleteByUserId(id);
    }

    // 9、分配用户
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void assignRole(UserAssignRoleDTO dto) {
        Long userId = dto.getUserId();
        //1 删除旧关联
        userRoleMapper.deleteByUserId(userId);
        List<Long> roleIdList = dto.getRoleIdList();
        if(CollUtil.isNotEmpty(roleIdList)){
            for (Long roleId : roleIdList) {
                UserRole ur = new UserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                userRoleMapper.insert(ur);
            }
        }
    }

    // 10、根据用户id查询已分配角色id集合
    @Override
    public List<Long> getRoleIdsByUserId(Long userId) {
        if(userId == null){return Collections.emptyList();}
        return userRoleMapper.selectRoleIdByUserId(userId);
    }

    // 11.1 校验用户是否存在
    public void validUsernameExist(String username){
        User user = userMapper.selectOneByUsername(username);
        if (user==null){
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
    }

    // 11.2 参数校验 校验用户名重名                         使用场景：并发注册、导入批量用户、重复注册提交
    public void validUsernameUnique(String username){
        User user = userMapper.selectOneByUsername(username);
        if (user!=null){
            throw new BusinessException(ResultCode.USERNAME_EXIST);
        }
    }

    // 11.5 校验用户状态是否禁用
    public void validUserStatus(User user){
        if (user.getStatus()== UserEnum.STATUS_FORBIDDEN.getCode()){
            throw new BusinessException(ResultCode.USER_STATUS_FORBIDDEN);
        }
    }




}
