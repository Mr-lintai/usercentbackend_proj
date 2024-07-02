package com.yupi.usercent.service;


import com.yupi.usercent.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;

/**
 * @author lintai
 * @version 1.0
 */
@SpringBootTest
class UserServiceTest {

    @Resource
    private UserService userService;

    void testDigest(){
        String newPassword = DigestUtils.md5DigestAsHex(("abcd"+"mypassword").getBytes());
        System.out.println(newPassword);
    }

    @Test
    public void testAddUser(){
        User user = new User();
        user.setUsername("dogyupi");
        user.setUserAccount("123");
        user.setAvatarUrl("https://5b0988e595225.cdn.sohucs.com/images/20200326/31b30019116546d7b81a773c8b32f0d9.jpeg");
        user.setGender(0);
        user.setUserPassword("xxx");
        user.setPhone("123");
        user.setEmail("456");
        boolean result = userService.save(user);
        System.out.println(user.getId());
        Assertions.assertTrue(result);
    }

    @Test
    void userRegister() {
        //密码为空
        String userAccount = "yupi";
        String userPassword = "";
        String checkPassword = "123456";
        String planetCode = "1";
        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        Assertions.assertEquals(-1, result);
        //账户长度
        userAccount = "yu";
        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        Assertions.assertEquals(-1, result);
        //密码长度
        userAccount = "yupi";
        userPassword = "123456";
        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        Assertions.assertEquals(-1, result);
        //特殊字符
        userAccount = "yu pi";
        userPassword = "12345678";
        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        Assertions.assertEquals(-1, result);
        //checkpwd不一致
        checkPassword = "123456789";
        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        Assertions.assertEquals(-1, result);
        //账户不重复
        userAccount = "dogYupi";
        checkPassword = "12345678";
        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        Assertions.assertEquals(-1, result);

        userAccount = "yupitest";
        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        //Assertions.assertTrue(result>0);
        Assertions.assertEquals(-1, result);


    }
}