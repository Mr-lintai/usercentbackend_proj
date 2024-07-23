package com.yupi.usercent.service;

import com.yupi.usercent.common.BaseResponse;
import com.yupi.usercent.model.VO.UserVO;
import com.yupi.usercent.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author lintai
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2024-06-18 22:24:04
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param checkPassword 星球编号
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode);

    /**
     * 用户登录
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    User getSafetyUser(User originUser);

    int userLogout(HttpServletRequest request);

    List<User> searchUsersByTags(List<String> tagNameList);

    int updateUser(User user, User loginUser);

    /**
     * 获取当前用户信息
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    boolean isAdmin(HttpServletRequest request);

    boolean isAdmin(User loginUser);


    /**
     * 匹配用户
     * @param num
     * @param loginUser
     * @return
     */
    List<User> matchUser(long num, User loginUser);
}
