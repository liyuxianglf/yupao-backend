package com.yx.service;

import com.yx.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yx.model.requestDto.UserRegisterRequest;
import com.yx.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户服务
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userRegisterRequest   用户注册请求体
     * @return  注册用户的Id
     */
    long userRegister(UserRegisterRequest userRegisterRequest);

    /**
     * 用户登录
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param request
     * @return  脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    User getSafetyUser(User originUser);

    /**
     * 获取当前登录用户信息
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 退出登录
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 根据标签搜素用户
     * @param tagList   标签列表
     * @return  满足标签的用户集合
     */
    List<User> searchUsersByTags(List<String> tagList);

    /**
     * 匹配标签相似的用户
     * @param num
     * @param loginUser
     * @return
     */
    List<User> matchUsers(long num, User loginUser);

    /**
     * 判断用户是否是管理员
     * @param loginUser
     * @return
     */
    boolean isAdmin(User loginUser);

    /**
     * 获取用户的标签信息
     * @param userId
     * @return
     */
    List<String> getUserTags(Long userId);

    /**
     * 更新用户标签信息
     * @param id
     * @param tagList
     * @return
     */
    boolean updateUserTags(Long id,List<String> tagList);
}
