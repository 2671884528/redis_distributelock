package com.gyg.config.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 郭永钢
 * 全局返回异常
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GlobalException extends Exception{
    private Integer code;
    private String msg;

    /**
     * 通过系统错误码直接填入异常
     * @param systemcode
     * @return
     */
    public GlobalException(SystemCode systemcode) {
        this.code=systemcode.code;
        this.msg = systemcode.message;
    }
}
