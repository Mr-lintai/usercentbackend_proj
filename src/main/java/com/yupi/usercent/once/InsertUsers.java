package com.yupi.usercent.once;
import java.util.Date;

import com.yupi.usercent.mapper.UserMapper;
import com.yupi.usercent.model.domain.User;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

/**
 * @author lintai
 * @version 1.0
 */
@Component
public class InsertUsers {

    @Resource
    private UserMapper userMapper;

    /**
     * 定时，只执行一次，批量插入用户
     */
    //@Scheduled(initialDelay = 5000, fixedDelay = Long.MAX_VALUE)
    public void doInsertUsers(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 1000;
        for (int i = 0; i <INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("假鱼皮");
            user.setUserAccount("fakeyupi");
            user.setAvatarUrl("https://image.baidu.com/search/detail?ct=503316480&z=0&ipn=d&word=%E5%9B%BE%E7%89%87&step_word=&hs=0&pn=0&spn=0&di=7360350738658099201&pi=0&rn=1&tn=baiduimagedetail&is=0%2C0&istype=0&ie=utf-8&oe=utf-8&in=&cl=2&lm=-1&st=undefined&cs=816796642%2C794164318&os=1128932389%2C2380644539&simid=3469722456%2C193936970&adpicid=0&lpn=0&ln=1735&fr=&fmq=1720421895976_R&fm=&ic=undefined&s=undefined&hd=undefined&latest=undefined&copyright=undefined&se=&sme=&tab=0&width=undefined&height=undefined&face=undefined&ist=&jit=&cg=&bdtype=0&oriquery=&objurl=https%3A%2F%2Finews.gtimg.com%2Fom_bt%2FOGlQWfsaAoKkuCcMZ2o9IVEPqd-72DQy5EAN02XBHUwfYAA%2F641&fromurl=ippr_z2C%24qAzdH3FAzdH3Fgjo_z%26e3Bqq_z%26e3Bv54AzdH3F6wtgAzdH3FwAzdH3Fdadn8a8bAablZTaa&gsm=1e&rpstart=0&rpnum=0&islist=&querylist=&nojc=undefined&dyTabStr=MCwzLDEsMiw2LDQsNSw4LDcsOQ%3D%3D&lid=8517941895645139002");
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("123");
            user.setEmail("123@qq.com");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setPlanetCode("11111111");
            user.setTags("[]");
            userMapper.insert(user);
        }
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

}
