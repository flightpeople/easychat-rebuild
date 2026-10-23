package com.example.easychat.common;

/**
 * 错误码表（M6a）——数值与原版文档完全一致（协议兼容的一部分：
 * 客户端对错误码的分支判断不用改）。
 */
public enum ResultCode {

    SUCCESS("0", "Success"),
    PARAM_ERROR("10000", "参数 %s 异常"),
    DB_ERROR("10001", "数据库异常"),
    SYSTEM_ERROR("10002", "系统内部异常"),
    NO_SESSION("10003", "请求头中没有携带token"),
    INVALID_SESSION("10004", "请求token不合法"),
    EXP_SESSION("10005", "请求的token已过期"),
    NOT_FRIEND("10006", "修改密码失败，旧密码错误"),
    NOT_MEMBER("10008", "您不是该群的成员，不能进行该操作");

    private final String code;
    private final String desc;

    ResultCode(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
