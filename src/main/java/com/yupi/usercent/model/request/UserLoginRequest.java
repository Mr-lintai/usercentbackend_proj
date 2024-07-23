package com.yupi.usercent.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lintai
 * @version 1.0
 */
@Data
public class UserLoginRequest implements Serializable {


    private static final long serialVersionUID = -6406705185281477488L;

    private String userAccount;
    private String userPassword;

}
