package com.yupi.usercent.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lintai
 * @version 1.0
 */
@Data
public class UserRegisterRequest implements Serializable {


    private static final long serialVersionUID = -4089946928387149342L;
    private String userAccount;
    private String userPassword;
    private String checkPassword;
    private String planetCode;

}
