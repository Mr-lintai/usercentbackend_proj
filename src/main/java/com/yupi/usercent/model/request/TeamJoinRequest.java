package com.yupi.usercent.model.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户加入队伍
 * @author lintai
 * @version 1.0
 */
@Data
public class TeamJoinRequest implements Serializable {
    /**
     * 队伍id
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;


    private static final long serialVersionUID = 1L;

}
