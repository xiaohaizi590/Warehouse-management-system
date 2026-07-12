package net.togogo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Size(min = 3, max = 20, message = "用户名长度必须在3到20个字符之间")
    private String username;

    @Email(message = "邮箱格式不正确")
    @Size(max = 50, message = "邮箱长度不能超过50个字符")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Size(min = 11, max = 11, message = "手机号长度必须为11位")
    private String phone;
    // 角色字段 —— 仅管理员可修改，普通用户传此字段将被忽略
    @Pattern(regexp = "^(ADMIN|USER)$", message = "角色值只能是 ADMIN和USER ")
    private String role;


}