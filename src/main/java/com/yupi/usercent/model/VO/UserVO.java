package com.yupi.usercent.model.VO;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户包装类（脱敏）
 * @author lintai
 * @version 1.0
 */
@Data
public class UserVO implements Serializable{

    /**
     *用户id
     */
    private long id;

    /**
     *用户名
     */
    private String username;

    /**
     * 账户名字
     */
    private String userAccount;

    /**
     * 人物图像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户状态，默认为0正常，（可能出现封号等情况）
     */
    private Integer userStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 0-普通用户 1-管理员 2
     */
    private Integer userRole;

    /**
     * 星球编号
     */
    private String planetCode;

    /**
     * 标签列表
     */
    private String tags;

    private static final long serialVersionUID = 1L;
}
