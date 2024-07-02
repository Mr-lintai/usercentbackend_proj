package com.yupi.usercent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.usercent.common.ErrorCode;
import com.yupi.usercent.exception.BussinessException;
import com.yupi.usercent.model.domain.User;
import com.yupi.usercent.service.UserService;
import com.yupi.usercent.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.yupi.usercent.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 针对表【user(用户)】的数据库操作Service实现
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Resource
    private UserMapper userMapper;

    //盐值，混淆密码
    private static final String SALT = "yupi";


    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        //1校验；使用apache工具类
        if(StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)){
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        if(userAccount.length() < 4){
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        if(userPassword.length() < 8 || checkPassword.length() < 8){
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        if(planetCode.length() > 5){
            throw new BussinessException(ErrorCode.PARAMS_ERROR);}

        //账户不能包含特殊字符
        String regEx="[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(regEx).matcher(userAccount);
        if(matcher.find()){//匹配到特殊字符报错
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "存在特殊字符");
        }
        //密码和校验码相同
        if(!userPassword.equals(checkPassword)){
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "密码和校验码不同");
        }

        //账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //指定列名，判断是否等于userAccount
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if(count > 0){
            throw new BussinessException(ErrorCode.PARAMS_ERROR,"账户名重复");
        }
        //星球编号不能重复
        queryWrapper = new QueryWrapper<>();
        //指定列名，判断是否等于userAccount
        queryWrapper.eq("userAccount", userAccount);
        count = userMapper.selectCount(queryWrapper);
        if(count > 0){
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "星球编号重复");
        }

        //2加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        boolean saveResult =this.save(user);
        if(!saveResult){
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "插入数据失败");
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {

        //1校验；使用apache工具类
        if(StringUtils.isAnyBlank(userAccount, userPassword)){
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        if(userAccount.length() < 4){
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        if(userPassword.length() < 8){
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }

        //账户不能包含特殊字符
        String regEx="[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(regEx).matcher(userAccount);
        if(matcher.find()){//匹配到特殊字符报错
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "匹配到特殊字符报错");
        }

        //查询用户
        //加密密码用于查询
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //注意排除逻辑删除用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);

        //用户不存在
        if(user == null){
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }

        //3脱敏，如果不脱敏，前端可以看到所有的用户信息
        User safetyUser =getSafetyUser(user);

        //4记录用户的登录态，登录态的用户信息也是脱敏的
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);

        return safetyUser;
    }

    /**
     * 用户脱敏
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser){
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        //密码不返回
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setUserStatus(0);
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setUserRole(originUser.getUserRole());
        //更新时间不返回
        //是否被删除与业务无关，不返回
        return safetyUser;
    }

    @Override
    public int userLogout(HttpServletRequest request) {
        //移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }
}




