package com.lc.finalexam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterDTO {
    @NotBlank(message = "user.username.notblank")
    @Size(min = 4, max = 20, message = "user.username.size")
    private String username;

    @NotBlank(message = "user.password.notblank")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,20}$", message = "user.password.pattern")
    private String password;

    private String email;

    @NotBlank(message = "user.phone.notblank")
    @Pattern(regexp = "^\\d{11}$", message = "user.phone.pattern")
    private String phone;
} 