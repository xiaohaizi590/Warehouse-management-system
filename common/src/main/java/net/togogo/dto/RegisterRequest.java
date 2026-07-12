package net.togogo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min=3,max = 50,message = "用户名长度必须在3到50个字符之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min=6,max = 20,message = "密码长度必须在6到20个字符之间")
    private String password;

    @NotBlank(message = "手机号不能为空")
    @Size(min=11,max = 11,message = "手机号长度必须为11个字符")
    private String phone;
}
