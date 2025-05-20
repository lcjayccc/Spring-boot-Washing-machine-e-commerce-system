package com.lc.finalexam.service;

import com.lc.finalexam.entity.User;

public interface UserService {
    User login(String username, String password);
    User getUserByUsername(String username);
}