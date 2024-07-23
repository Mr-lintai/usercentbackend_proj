package com.yupi.usercent.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.usercent.common.BaseResponse;
import com.yupi.usercent.common.ErrorCode;
import com.yupi.usercent.common.ResultUtils;
import com.yupi.usercent.exception.BussinessException;
import com.yupi.usercent.model.VO.UserVO;
import com.yupi.usercent.model.domain.User;
import com.yupi.usercent.model.request.UserLoginRequest;
import com.yupi.usercent.model.request.UserRegisterRequest;
import com.yupi.usercent.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.yupi.usercent.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author lintai
 * @version 1.0
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:3000"})
@Slf4j
//@SuppressWarnings({"all"})
public class UserController {

    @Resource
    private UserService userService;
    @Resource
    private RedisTemplate redisTemplate;

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
        if (!userService.isAdmin(request)) {
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

    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(List<String> tagNameList){
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(userList);
    }

//    @GetMapping("/recommend")
//    public BaseResponse<List<User>> recommendUsers(List<String> tagNameList){
//        if(CollectionUtils.isEmpty(tagNameList)){
//            throw new BussinessException(ErrorCode.PARAMS_ERROR);
//        }
//        List<User> userList = userService.searchUsersByTags(tagNameList);
//        return ResultUtils.success(userList);
//    }

    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(int pageSize, int pageNum, HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        //如果有缓存，直接读缓存
        String redisKey = String.format("yupao:user:recommed:%s", loginUser.getId());
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        //直接返回结果并没有使用pageSize和pageNum两个参数？？？
        Page<User> userPage = (Page<User>) valueOperations.get(redisKey);
        if(userPage != null){
            return ResultUtils.success(userPage);
        }
        //无缓存，查数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        userPage = userService.page(new Page<>(pageNum, pageSize), queryWrapper);
        //写缓存
        try {
            valueOperations.set(redisKey, userPage, 600000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("redis set key error", e);
        }
        return ResultUtils.success(userPage);
}

    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        //1、校验参数是否为空
        if (user == null) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        //2、校验权限
        User loginUser = userService.getLoginUser(request);

        //3、触发更新
        int result = userService.updateUser(user, loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        //仅管理员可操作
        if (!userService.isAdmin(request)) {
            throw new BussinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result =  userService.removeById(id);
        return ResultUtils.success(result);
    }

    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(long num, HttpServletRequest request){
        //涉及查数据一定要限制页数
        if(num<=0 || num>20){
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.matchUser(num, user));
    }

}
