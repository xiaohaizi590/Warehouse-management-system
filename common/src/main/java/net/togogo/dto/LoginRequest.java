package net.togogo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
//登录请求DTO，定义前端传入的登录请求参数
public class LoginRequest {
    //接住用户请求
    @NotBlank(message = "账号不能为空")
    @Size(min = 3, max = 20, message = "手机号或者账号进行登录")
    private String account;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6到20个字符之间")
    private String password;



}
