package com.yupi.usercent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yupi.usercent.common.BaseResponse;
import com.yupi.usercent.common.ErrorCode;
import com.yupi.usercent.exception.BussinessException;
import com.yupi.usercent.model.VO.UserVO;
import com.yupi.usercent.model.domain.User;
import com.yupi.usercent.service.UserService;
import com.yupi.usercent.mapper.UserMapper;
import com.yupi.usercent.utils.AlgorithmUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.yupi.usercent.constant.UserConstant.ADMIN_ROLE;
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

    @Deprecated
    private List<User> searchUsersByTagsBySQL(List<String> tagNameList){
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 拼接 and 查询
        //like ‘%java’ and like '%Python'
        for(String tagName : tagNameList){
            queryWrapper = queryWrapper.like("tags", tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

    public List<User> searchUsersByTags(List<String> tagNameList){
        //1、先查询所有用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        //2、在内存中判断是否有包含要求的标签
        return userList.stream().filter(user->{
            String tagsStr = user.getTags();
            if(StringUtils.isNotBlank(tagsStr)){
                return false;
            }
            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>(){}.getType());
            for(String tagName : tagNameList){
                if(!tempTagNameSet.contains(tagName)){
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }

    @Override
    public int updateUser(User user, User loginUser) {
        long userId = user.getId();
        if(userId <= 0){
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        //todo 补充校验，如果用户没有传任何要更新的值，就直接报错，不用执行更新语句
        //仅管理员和自己可修改
        //如果是管理员，允许更新任意用户
        //如果不是管理员，仅允许更新自己当前信息
        if(isAdmin(loginUser) && userId != loginUser.getId()){
            throw new BussinessException(ErrorCode.NO_AUTH);
        }
        User oldUser = userMapper.selectById(userId);
        if(oldUser == null){
            throw new BussinessException(ErrorCode.NULL_ERROR);
        }
        return userMapper.updateById(user);//i为受影响行数
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        if(request == null){
            return null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if(userObj == null){
            throw new BussinessException(ErrorCode.NO_AUTH);
        }
        return (User)userObj;
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
        safetyUser.setTags(originUser.getTags());
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

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    public boolean isAdmin(HttpServletRequest request) {
        //仅管理员可操作
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    public boolean isAdmin(User loginUser) {
        //仅管理员可操作
        return loginUser != null && loginUser.getUserRole() == ADMIN_ROLE;
    }

    @Override
    public List<User> matchUser(long num, User loginUser) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "select");
        queryWrapper.isNotNull("tag");
        List<User> userList = this.list();
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>(){}.getType());
        //用户列表的下标=》相似度
        SortedMap<Integer, Long> indexDistanceMap = new TreeMap<>(Comparator.comparing(a->a));
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            //无标签
            if(StringUtils.isBlank(userTags)){
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>(){}.getType());
            //计算分数
            long distance = AlgorithmUtils.editDistance(tagList, userTagList);
            indexDistanceMap.put(i, distance);
        }
        List<Integer> maxDistanceIndexList = indexDistanceMap.keySet().stream().limit(num).collect(Collectors.toList());
        List<User> userVOList = maxDistanceIndexList.stream()
                .map(index-> getSafetyUser(userList.get(index)))
                .collect(Collectors.toList());
        return userVOList;
    }

}




