package com.example.easychat.common;

/**
 * 统一响应格式（M6a）——与原版对齐：{code, desc, data}。
 *
 * 为什么 REST 侧要统一包装：客户端（浏览器插件/Postman）可以用同一套
 * 解析逻辑处理所有接口；错误码语义化（code=10004 就是 token 不合法），
 * 比只用 HTTP 状态码表达力强（HTTP 200 + code=10004 vs HTTP 401）。
 */
public class Result<T> {

    private String code;
    private String desc;
    private T data;

    public Result() {
    }

    public Result(String code, String desc, T data) {
        this.code = code;
        this.desc = desc;
        this.data = data;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getDesc(), data);
    }

    public static <T> Result<T> error(ResultCode rc) {
        return new Result<>(rc.getCode(), rc.getDesc(), null);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
