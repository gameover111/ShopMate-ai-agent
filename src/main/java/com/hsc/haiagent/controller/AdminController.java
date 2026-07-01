package com.hsc.haiagent.controller;

import com.hsc.haiagent.entity.User;
import com.hsc.haiagent.repository.UserRepository;
import com.hsc.haiagent.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理员控制器 — 用户管理
 */
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Resource
    private UserService userService;

    @Resource
    private UserRepository userRepository;

    /**
     * 获取用户列表
     */
    @GetMapping("/users")
    public List<Map<String, Object>> listUsers() {
        return userService.getAllUsers().stream()
                .map(u -> Map.<String, Object>of(
                        "id", u.getId(),
                        "username", u.getUsername(),
                        "email", u.getEmail(),
                        "role", u.getRole(),
                        "createdAt", u.getCreatedAt(),
                        "updatedAt", u.getUpdatedAt()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 修改用户角色（管理员/用户切换）
     */
    @PutMapping("/users/{id}/role")
    public Map<String, Object> updateUserRole(@PathVariable Long id,
                                              @RequestBody Map<String, String> body) {
        User user = userService.updateUserRole(id, body.get("role"));
        return Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "role", user.getRole(),
                "message", "角色已更新为 " + body.get("role")
        );
    }

    /**
     * 管理员编辑用户信息（用户名/邮箱）
     */
    @PutMapping("/users/{id}/profile")
    public Map<String, Object> updateUserProfile(@PathVariable Long id,
                                                  @RequestBody Map<String, String> body) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        if (body.containsKey("username") && !body.get("username").isBlank()) {
            user.setUsername(body.get("username"));
        }
        if (body.containsKey("email")) {
            user.setEmail(body.get("email"));
        }
        userRepository.save(user);
        return Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "message", "用户信息已更新"
        );
    }

    /**
     * 重置用户密码为 init123456
     */
    @PutMapping("/users/{id}/password")
    public Map<String, String> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return Map.of("message", "密码已重置为 init123456");
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/users/{id}")
    public Map<String, String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Map.of("message", "用户已删除");
    }
}
