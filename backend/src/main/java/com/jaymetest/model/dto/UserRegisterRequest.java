package com.jaymetest.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求
 */
@Data
public class UserRegisterRequest {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 10, message = "密码长度为 6–10 位")
    private String password;

    @NotBlank(message = "昵称不能为空")
    @Size(min = 2, max = 10, message = "昵称长度为 2–10 个字符")
    private String nickname;
}
