//package com.zh.hengyi.config.mapstruct;
//
//import com.zh.hengyi.admin.model.dto.user.UserLoginDTO;
//import com.zh.hengyi.admin.model.dto.user.UserRegisterDTO;
//import com.zh.hengyi.admin.model.vo.user.UserLoginVO;
//import com.zh.hengyi.model.entity.User;
//import org.mapstruct.Mapper;
//import org.mapstruct.factory.Mappers;
//
//@Mapper(componentModel = "spring") // componentModel=spring：交给Spring管理
//public interface UserConvert {
//
//    //必须要写，不写不会注入UserConvert生成实例，导致转换失败
//    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);
//
//    // Entity → LoginVO
//    UserLoginVO toLoginVO(User user);
//
////    // 忽略某个字段，不赋值
////    @Mapping(target = "password", ignore = true)
////    UserLoginVO toVoIgnorePwd(User user);
//
//    // RegisterDTO → Entity（注册场景）
//    User toEntity(UserRegisterDTO dto);
//
//    // LoginDTO → Entity（登录场景）
//    User toEntity(UserLoginDTO dto);
//
//
//
//
//
//
////    /**
////     * 字段名称不一致手动映射
////     * source：源对象字段
////     * target：目标对象字段
////     */
////    @Mapping(source = "userName", target = "username")
////    UserVO toVO(User entity);
//
//
//}