package com.gyg.config.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.net.SocketTimeoutException;

/**
 * @author 郭永钢
 */
@ControllerAdvice
public class GlobalExceptionHandle {

    @ExceptionHandler(value = GlobalException.class)
    @ResponseBody
    public String handle(HttpServletRequest request, GlobalException e) {
        return e.getMsg();
    }

    @ExceptionHandler(value = SocketTimeoutException.class)
    @ResponseBody
    public String handle(HttpServletRequest request, SocketTimeoutException e) {
        return e.getMessage();
    }
}

