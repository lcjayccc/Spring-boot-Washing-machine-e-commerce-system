package com.lc.finalexam.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserProfileDTO {

    private String phone;

    
    private String email;

    private String avatarUrl;
} 