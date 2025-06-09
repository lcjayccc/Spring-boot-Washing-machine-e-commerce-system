package com.lc.finalexam.controller;

import com.lc.finalexam.entity.User;
import com.lc.finalexam.service.UserService;
import com.lc.finalexam.util.CaptchaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@Controller
public class LoginController {
    @Autowired
    private UserService userService;

    @GetMapping("/unified-login")
    public String unifiedLoginPage(@RequestParam(required = false) String role, Model model, HttpSession session) {
        try {
            // 生成验证码
            String captchaCode = CaptchaUtil.generateCaptchaCode();
            String captchaBase64 = CaptchaUtil.generateBase64Captcha(captchaCode);
            
            // 将验证码存入会话
            session.setAttribute("captchaCode", captchaCode);
            model.addAttribute("captchaImage", captchaBase64);
            
            return "unified_login";
        } catch (IOException e) {
            // 验证码生成失败处理
            return "unified_login";
        }
    }

    // 刷新验证码的API
    @GetMapping(value = "/captcha/refresh", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String refreshCaptcha(HttpSession session) {
        try {
            String captchaCode = CaptchaUtil.generateCaptchaCode();
            String captchaBase64 = CaptchaUtil.generateBase64Captcha(captchaCode);
            session.setAttribute("captchaCode", captchaCode);
            return captchaBase64;
        } catch (IOException e) {
            return "";
        }
    }

    @GetMapping("/login")
    public String loginPage() {
        return "redirect:/unified-login?role=admin";
    }

    @GetMapping("/user/login")
    public String userLoginPage() {
        return "redirect:/unified-login?role=user";
    }
    
    /**
     * 统一的登录处理方法
     */
    @PostMapping("/unified-login")
    public String unifiedLogin(@RequestParam String loginInput,
                             @RequestParam String password,
                             @RequestParam String userRole,
                             @RequestParam String captcha,
                             HttpSession session,
                             Model model) {
        
        // 验证码校验
        String sessionCaptcha = (String) session.getAttribute("captchaCode");
        if (sessionCaptcha == null || !sessionCaptcha.equalsIgnoreCase(captcha)) {
            model.addAttribute("errorMessage", "验证码错误");
            String errorKey = "user".equals(userRole) ? "loginKey" : "username";
            model.addAttribute(errorKey, loginInput);
            
            try {
                // 重新生成验证码
                String newCaptchaCode = CaptchaUtil.generateCaptchaCode();
                String newCaptchaBase64 = CaptchaUtil.generateBase64Captcha(newCaptchaCode);
                session.setAttribute("captchaCode", newCaptchaCode);
                model.addAttribute("captchaImage", newCaptchaBase64);
            } catch (IOException e) {
                // 忽略验证码生成错误
            }
            
            return "unified_login";
        }
        
        // 验证码使用后立即清除，防止重复使用
        session.removeAttribute("captchaCode");
        
        // 根据登录键查找用户
        User user = userService.getUserByUsername(loginInput);
        if (user == null) {
            // 尝试其他方式查询用户（邮箱、手机号、ID等）
            try {
                Integer userId = Integer.parseInt(loginInput);
                user = userService.getUserById(userId);
            } catch (NumberFormatException e) {
                // 不是数字ID，忽略异常
            }
        }
        
        // 验证用户身份
        if (user != null && user.getPassword().equals(password)) {
            // 检查角色是否匹配
            String actualRole = user.getRole();
            
            // 角色匹配检查：不允许用户登录界面使用管理员账户
            if ("user".equals(userRole) && "ADMIN".equals(actualRole)) {
                model.addAttribute("errorMessage", "管理员账户请通过管理员入口登录");
                model.addAttribute("loginKey", loginInput);
                
                // 重新生成验证码
                try {
                    String newCaptchaCode = CaptchaUtil.generateCaptchaCode();
                    String newCaptchaBase64 = CaptchaUtil.generateBase64Captcha(newCaptchaCode);
                    session.setAttribute("captchaCode", newCaptchaCode);
                    model.addAttribute("captchaImage", newCaptchaBase64);
                } catch (IOException e) {
                    // 忽略验证码生成错误
                }
                
                return "unified_login";
            }
            
            // 角色匹配检查：不允许管理员登录界面使用普通用户账户
            if ("admin".equals(userRole) && !"ADMIN".equals(actualRole)) {
                model.addAttribute("errorMessage", "此账户没有管理员权限");
                model.addAttribute("username", loginInput);
                
                // 重新生成验证码
                try {
                    String newCaptchaCode = CaptchaUtil.generateCaptchaCode();
                    String newCaptchaBase64 = CaptchaUtil.generateBase64Captcha(newCaptchaCode);
                    session.setAttribute("captchaCode", newCaptchaCode);
                    model.addAttribute("captchaImage", newCaptchaBase64);
                } catch (IOException e) {
                    // 忽略验证码生成错误
                }
                
                return "unified_login";
            }
            
            // 根据实际角色跳转
            if ("ADMIN".equals(actualRole)) {
                // 管理员登录成功
                session.setAttribute("admin", user);
                return "redirect:/category/list";
            } else {
                // 普通用户登录成功
                session.setAttribute("user", user);
                return "redirect:/user/index";
            }
        }
        
        // 登录失败处理
        String errorKey = "user".equals(userRole) ? "loginKey" : "username";
        model.addAttribute("errorMessage", "账号或密码错误");
        model.addAttribute(errorKey, loginInput);
        
        // 登录失败时重新生成验证码
        try {
            String newCaptchaCode = CaptchaUtil.generateCaptchaCode();
            String newCaptchaBase64 = CaptchaUtil.generateBase64Captcha(newCaptchaCode);
            session.setAttribute("captchaCode", newCaptchaCode);
            model.addAttribute("captchaImage", newCaptchaBase64);
        } catch (IOException e) {
            // 忽略验证码生成错误
        }
        
        return "unified_login";
    }

    // 保留原有的登录端点，但将它们重定向到统一登录处理
    @PostMapping("/login")
    public String adminLogin(@RequestParam String username,
                        @RequestParam String password,
                        @RequestParam(required = false) String captcha,
                        HttpSession session,
                        Model model) {
        // 管理员登录路径
        return unifiedLogin(username, password, "admin", captcha != null ? captcha : "", session, model);
    }

    @PostMapping("/user/login")
    public String userLogin(@RequestParam String loginKey,
                        @RequestParam String password,
                        @RequestParam(required = false) String captcha,
                        HttpSession session,
                        Model model) {
        // 用户登录路径
        return unifiedLogin(loginKey, password, "user", captcha != null ? captcha : "", session, model);
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/unified-login";
    }
}