package com.yupi.usercent.exception;

import com.yupi.usercent.common.BaseResponse;
import com.yupi.usercent.common.ErrorCode;
import com.yupi.usercent.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author lintai
 * @version 1.0
 */

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    //aop
    @ExceptionHandler(BussinessException.class)
    public BaseResponse bussinessExceptionHandler(BussinessException e) {
        log.error("bussinessException"+ e.getMessage(), e);
        return ResultUtils.error(e.getCode(),e.getMessage(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse runtimeExceptionHandler(BussinessException e) {
        log.error("runtimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage(), "");
    }

}
