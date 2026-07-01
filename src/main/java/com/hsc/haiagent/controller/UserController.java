package com.hsc.haiagent.controller;

import com.hsc.haiagent.entity.User;
import com.hsc.haiagent.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户控制器 — 个人信息管理
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    public Map<String, Object> getProfile(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        User user = userService.getUserById(userId);
        return Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "avatar", user.getAvatar(),
                "role", user.getRole(),
                "createdAt", user.getCreatedAt()
        );
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/profile")
    public Map<String, Object> updateProfile(Authentication auth,
                                              @RequestBody Map<String, String> body) {
        Long userId = (Long) auth.getPrincipal();
        User user = userService.updateProfile(userId, body.get("email"), body.get("avatar"));
        return Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "avatar", user.getAvatar(),
                "role", user.getRole()
        );
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Map<String, String> changePassword(Authentication auth,
                                              @RequestBody Map<String, String> body) {
        Long userId = (Long) auth.getPrincipal();
        userService.changePassword(userId, body.get("oldPassword"), body.get("newPassword"));
        return Map.of("message", "密码修改成功");
    }
}
