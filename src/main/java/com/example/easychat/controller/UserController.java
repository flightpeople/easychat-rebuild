package com.example.easychat.controller;

import com.example.easychat.common.Result;
import com.example.easychat.model.User;
import com.example.easychat.service.FriendService;
import com.example.easychat.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户查询 Controller（M6a）——只读。
 *
 * 写操作在 UserWriteController（M6b，读写分离）：
 * 查询接口通常无副作用、可缓存、可水平扩展；写接口涉及校验和事务——
 * 分开后各自的演进（如查询加缓存、写加审计）互不干扰。
 *
 * 当前登录用户从 request 属性取（拦截器放的），不从参数取——
 * 防越权：不能靠客户端传 userId 就说“我是谁”。
 */
@RestController
public class UserController {

    private final UserService userService;
    private final FriendService friendService;

    public UserController(UserService userService, FriendService friendService) {
        this.userService = userService;
        this.friendService = friendService;
    }

    /** GET /users 我的好友列表（联表带用户信息） */
    @GetMapping("/users")
    public Result<List<User>> friends(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        return Result.success(friendService.listFriends(userId));
    }

    /** GET /user/{userId} 用户信息（可以查他人） */
    @GetMapping("/user/{userId}")
    public Result<User> user(@PathVariable String userId) {
        return Result.success(userService.getById(userId));
    }
}
