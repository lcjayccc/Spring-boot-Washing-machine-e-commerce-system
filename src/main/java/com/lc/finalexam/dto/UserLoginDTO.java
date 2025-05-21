package com.lc.finalexam.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginDTO {
    @NotBlank(message = "user.loginkey.notblank")
    private String loginKey; // 用户名/邮箱/手机号

    @NotBlank(message = "user.password.notblank")
    private String password;
} 