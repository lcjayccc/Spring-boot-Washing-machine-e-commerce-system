package com.lc.finalexam.service.impl;

import com.lc.finalexam.entity.User;
import com.lc.finalexam.repository.UserRepository;
import com.lc.finalexam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepo;

    @Override
    public User login(String username, String password) {
        User user = userRepo.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepo.findByUsername(username);
    }
}