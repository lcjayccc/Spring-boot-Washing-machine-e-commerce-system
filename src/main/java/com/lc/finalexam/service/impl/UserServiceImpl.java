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

    @Override
    public User register(User user) {
        // 校验用户名、邮箱、手机号唯一性
        if (userRepo.findByUsername(user.getUsername()) != null) return null;
        if (user.getEmail() != null && userRepo.findByEmail(user.getEmail()) != null) return null;
        if (user.getPhone() != null && userRepo.findByPhone(user.getPhone()) != null) return null;
        // 默认角色
        user.setRole("USER");
        return userRepo.save(user);
    }

    @Override
    public User updateProfile(Integer userId, String email, String phone, String avatarUrl) {
        User user = userRepo.findById(userId).orElse(null);
        if (user == null) return null;
        if (email != null) user.setEmail(email);
        if (phone != null) user.setPhone(phone);
        if (avatarUrl != null) user.setAvatarUrl(avatarUrl);
        return userRepo.save(user);
    }

    @Override
    public User getUserById(Integer id) {
        return userRepo.findById(id).orElse(null);
    }
}