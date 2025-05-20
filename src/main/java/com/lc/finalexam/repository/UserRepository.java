package com.lc.finalexam.repository;

import com.lc.finalexam.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    // 按用户名查找
    User findByUsername(String username);

    // 按邮箱查找
    User findByEmail(String email);
}