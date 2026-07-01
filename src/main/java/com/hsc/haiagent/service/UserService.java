package com.hsc.haiagent.service;

import com.hsc.haiagent.entity.User;
import com.hsc.haiagent.repository.UserRepository;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Resource
    private UserRepository userRepository;

    @Resource
    private PasswordEncoder passwordEncoder;

    /**
     * 注册用户
     */
    public User register(String username, String password, String email) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("邮箱不能为空");
        }
        if (userRepository.existsByEmail(email.trim())) {
            throw new RuntimeException("该邮箱已被注册");
        }
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email.trim());
        user.setRole("USER");
        return userRepository.save(user);
    }

    /**
     * 用户登录验证（使用邮箱）
     */
    public User login(String email, String password) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("请输入邮箱");
        }
        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new RuntimeException("邮箱或密码错误"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("邮箱或密码错误");
        }
        return user;
    }

    /**
     * 获取用户信息
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    /**
     * 更新用户信息
     */
    public User updateProfile(Long userId, String email, String avatar) {
        User user = getUserById(userId);
        if (email != null) user.setEmail(email);
        if (avatar != null) user.setAvatar(avatar);
        return userRepository.save(user);
    }

    /**
     * 修改密码
     */
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getUserById(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * 获取所有用户（管理员）
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * 修改用户角色（管理员）
     */
    public User updateUserRole(Long userId, String role) {
        User user = getUserById(userId);
        user.setRole(role);
        return userRepository.save(user);
    }

    /**
     * 删除用户（管理员）
     */
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    /**
     * 管理员重置用户密码为 init123456
     */
    public void resetPassword(Long userId) {
        User user = getUserById(userId);
        user.setPassword(passwordEncoder.encode("init123456"));
        userRepository.save(user);
    }
}
