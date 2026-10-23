package com.example.easychat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.easychat.mapper.UserMapper;
import com.example.easychat.model.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 用户服务的 MySQL 实现（M6b：继承 ServiceImpl，获得 updateById/list）。
 */
@Service
public class MyBatisUserService extends ServiceImpl<UserMapper, User> implements UserService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User register(String userName, String rawPassword, String nickName) {
        if (existsByName(userName)) {
            return null;
        }
        String userId = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10).toUpperCase();
        User user = new User(userId, userName, passwordEncoder.encode(rawPassword), nickName);
        baseMapper.insert(user);
        System.out.println("[UserService] 注册成功(MySQL): userName=" + userName + ", userId=" + userId);
        return user;
    }

    @Override
    public User getByName(String userName) {
        return baseMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUserName, userName));
    }

    @Override
    public User getById(String userId) {
        return baseMapper.selectById(userId);
    }

    @Override
    public boolean existsByName(String userName) {
        return baseMapper.exists(new LambdaQueryWrapper<User>()
                .eq(User::getUserName, userName));
    }
}
