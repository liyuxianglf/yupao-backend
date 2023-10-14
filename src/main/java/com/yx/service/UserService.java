package com.yx.service;

import com.yx.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yx.model.requestDto.UserRegisterRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author lige
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2023-09-12 11:16:38
*/
public interface UserService extends IService<User> {

    //注册功能实现
    long userRegister(UserRegisterRequest userRegisterRequest);

    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    User getSafetyUser(User originUser);

    User getLoginUser(HttpServletRequest request);

    int userLogout(HttpServletRequest request);

    List<User> searchUsersByTags(List<String> tagList);

    List<User> matchUsers(long num, User loginUser);


    boolean isAdmin(HttpServletRequest request);
    boolean isAdmin(User loginUser);
}
