package net.togogo.common;

import lombok.Getter;

@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "系统错误"),

    // 业务状态码
    USERNAME_EXIST(1001, "用户名已存在"),
    USER_NOT_FOUND(1002, "用户不存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    TOKEN_INVALID(1004, "Token 无效"),
    TOKEN_EXPIRED(1005, "Token 已过期"),
    PHONE_EXIST(1006, "手机号已存在"),
    EMAIL_EXIST(1007, "邮箱已存在"),

    // 图书相关状态码
    BOOK_ISBN_EXIST(2001, "ISBN已存在"),
    BOOK_NOT_AVAILABLE(2002, "图书库存不足"),
    BOOK_ALREADY_BORROWED(2003, "图书已被借阅"),
    BOOK_BORROWED_CANNOT_DELETE(2004, "图书已被借阅，无法删除"),

    // 借阅记录相关状态码
    RECORD_NOT_BORROWED(3001, "记录未处于借阅状态"),
    MAX_RENEW_COUNT_EXCEEDED(3002, "已达到最大续借次数");

    private final Integer code;//状态码
    private final String message;//状态码对应的消息

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
