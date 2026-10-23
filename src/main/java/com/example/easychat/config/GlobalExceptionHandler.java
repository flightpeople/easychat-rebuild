package com.example.easychat.config;

import com.example.easychat.common.Result;
import com.example.easychat.common.ResultCode;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理（M6b）。
 *
 * 【对拍看点】原版 /system/checkVersion 缺必填参数时返回 10002“系统内部错误”
 * （我们测出的 BUG-004）——参数错误应该是 10000。复刻版在这里显式转换：
 * MissingServletRequestParameterException → 10000。
 * 全局异常处理器的另一个价值：没有它，异常会漏成 Spring 默认白页/500，
 * 客户端拿到一堆 HTML 而不是统一 JSON——REST 接口的基本体面。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<String> handleMissingParam(MissingServletRequestParameterException e) {
        return Result.error(ResultCode.PARAM_ERROR);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<String> handleNotReadable(HttpMessageNotReadableException e) {
        return Result.error(ResultCode.PARAM_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public Result<String> handleOther(Exception e) {
        e.printStackTrace();
        return Result.error(ResultCode.SYSTEM_ERROR);
    }
}
