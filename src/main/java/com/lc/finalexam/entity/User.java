package com.lc.finalexam.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "user")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    private String email;
    private String phone;

    private String avatarUrl; // 头像路径

    @Column(nullable = false)
    private String role; // "ADMIN" or "USER"
}
