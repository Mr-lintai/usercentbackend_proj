package com.yupi.usercent.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yupi.usercent.common.BaseResponse;
import com.yupi.usercent.common.ErrorCode;
import com.yupi.usercent.common.ResultUtils;
import com.yupi.usercent.exception.BussinessException;
import com.yupi.usercent.model.domain.User;
import com.yupi.usercent.model.domain.request.UserLoginRequest;
import com.yupi.usercent.model.domain.request.UserRegisterRequest;
import com.yupi.usercent.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static com.yupi.usercent.constant.UserConstant.ADMIN_ROLE;
import static com.yupi.usercent.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author lintai
 * @version 1.0
 */
@RestController
@RequestMapping("/user")
//@SuppressWarnings({"all"})
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {

        if (userRegisterRequest == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }

        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();

        if (StringUtils.isAnyEmpty(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        //userService.userRegister(userAccount, userPassword, checkPassword);
        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {

        if (userLoginRequest == null) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }

        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        if (StringUtils.isAnyEmpty(userAccount, userPassword)) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }

        User user =  userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    @PostMapping("/logout")
    public BaseResponse userLogout(HttpServletRequest request) {

        if (request == null) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }

        Integer result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrent(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if(currentUser == null){
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        long userId = currentUser.getId();
        // TODO 校验用户是否合法
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    @PostMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {

        //仅管理员可查
        if (!isAdmin(request)) {
            //return new ArrayList<>();
            throw new BussinessException(ErrorCode.NO_AUTH);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList.stream().map(user-> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);

    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        //仅管理员可操作
        if (!isAdmin(request)) {
            throw new BussinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result =  userService.removeById(id);
        return ResultUtils.success(result);
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    private boolean isAdmin(HttpServletRequest request) {
        //仅管理员可操作
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

}
