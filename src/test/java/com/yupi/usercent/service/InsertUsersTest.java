package com.yupi.usercent.service;

import com.yupi.usercent.model.domain.User;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author lintai
 * @version 1.0
 */
@SpringBootTest
public class InsertUsersTest {


    private UserService userService;
    //时间单位设置为分钟，线程多久没用就回收掉
    //区分两种情况
    //任务多做加减乘除。。。CPU 密集型。分配的核心线程数 = CPU - 1（留一个核心运行主线程）
    //IO密集型。。。读写磁盘耗时多。分盘的核心线程数可以大于CPU核数
    private ExecutorService executorService = new ThreadPoolExecutor(60, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));

    @Test
    public void doInsertUsers(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 1000;
        List<User> userList = new ArrayList<User>();
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
            //add data
            userList.add(user);
        }
        userService.saveBatch(userList, 100);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    @Test
    public void doConcurrencyInsertUsers(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 100000;
        // 分十组
        int j = 0;
        //任务数组
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (int i = 0; i <10; i++) {
            //先把数据分配好，再完成任务
            //转成线程安全的集合类型
            List<User> userList = Collections.synchronizedList(new ArrayList<User>());
            while(true)
            {
                j++;
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
                //add data
                userList.add(user);
                if(j%10000 == 0){
                    break;
                }
            }
            //异步执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(()->{
                System.out.println("threadname:" + Thread.currentThread().getName());
                userService.saveBatch(userList, 10000);
            }, executorService);
            //加入10个异步任务
            futureList.add(future);
        }
        //执行异步任务
        //join阻塞一下，等10个异步任务完成后，再执行下去
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();

        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

}
