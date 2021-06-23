package com.gyg.config.exception;


public enum SystemCode {

    /**
     * OK
     */
    OK(1, "成功"),

    /**
     * AccessTokenError
     */
    AccessTokenError(400, "令牌失效"),

    /**
     * UNAUTHORIZED
     */
    UNAUTHORIZED(401, "用户未登录"),

    /**
     * AuthError
     */
    AuthError(402, "用户账号或密码错误"),

    /**
     * InnerError
     */
    InnerError(500, "系统内部错误"),

    /**
     * ParameterValidError
     */
    ParameterValidEGlobalExceptionHandlerror(501, "参数错误"),

    /**
     * ParameterValidNotExit
     */
    ParameterValidNotExit(502, "查询不存在"),
    /**
     * ServiceCall
     */
    ServiceCall(505, "调用的服务无法响应"),

    /**
     * AccessDenied
     */
    AccessDenied(502, "用户没有权限"),
    ;

    int code;
    String message;

    SystemCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public SystemCode setCode(int code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public SystemCode setMessage(String message) {
        this.message = message;
        return this;
    }

    SystemCode() {
    }


}
