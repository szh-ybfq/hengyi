package com.zh.hengyi.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zh.hengyi.admin.model.dto.user.UserLoginDTO;
import com.zh.hengyi.admin.model.dto.user.UserRegisterDTO;
import com.zh.hengyi.admin.model.vo.user.UserLoginVO;
import com.zh.hengyi.admin.service.UserService;
import com.zh.hengyi.common.enums.user.UserEnum;
import com.zh.hengyi.common.exception.BusinessException;
import com.zh.hengyi.common.result.ResultCode;
//import com.zh.hengyi.config.mapstruct.UserConvert;
import com.zh.hengyi.config.sercurity.login.LoginUser;
import com.zh.hengyi.config.sercurity.utils.jwt.JwtUtil;
import com.zh.hengyi.mapper.UserMapper;
import com.zh.hengyi.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

import static com.zh.hengyi.common.constant.AuthConstant.*;

@Service
@RequiredArgsConstructor // 相比@Autowired好处，见笔记
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    // 1、注册（新增插入，不用判断存在）
    /*
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
        // 1 封装认证对象、调用认证
        UsernamePasswordAuthenticationToken authToken =new UsernamePasswordAuthenticationToken(login.getUsername(),login.getPassword());
        Authentication auth = authenticationManager.authenticate(authToken);
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
        vo.setPermissionList(loginUser.getPermissions());
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

    // 3、修改密码

    // 9.1 校验用户是否存在
    public void validUsernameExist(Long id){
        User user = userMapper.selectById(id);
        if (user==null){
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
    }

    // 9.2 参数校验 校验用户名重名                         使用场景：并发注册、导入批量用户、重复注册提交
    public void validUsernameUnique(String username){
        User user = userMapper.selectOneByUsername(username);
        if (user!=null){
            throw new BusinessException(ResultCode.USERNAME_EXIST);
        }
    }

    // 9.5 校验用户状态是否禁用
    public void validUserStatus(User user){
        if (user.getStatus()== UserEnum.STATUS_FORBIDDEN.getCode()){
            throw new BusinessException(ResultCode.USER_STATUS_FORBIDDEN);
        }
    }

}
