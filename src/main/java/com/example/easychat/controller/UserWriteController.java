package com.example.easychat.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.easychat.common.Result;
import com.example.easychat.mapper.FriendMapper;
import com.example.easychat.model.User;
import com.example.easychat.model.UserFriend;
import com.example.easychat.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户修改类 Controller（M6b）。
 *
 * 与 M6a 的查询类分工：这里全部是“写操作”——改资料、改密码、改备注。
 * 写操作的共同纪律：当前用户只能改自己（userId 从拦截器放的 request 属性取，
 * 不信客户端传参）。
 */
@RestController
public class UserWriteController {

    private final UserService userService;
    private final FriendMapper friendMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserWriteController(UserService userService, FriendMapper friendMapper) {
        this.userService = userService;
        this.friendMapper = friendMapper;
    }

    /** PUT /user/update 修改个人资料（昵称/签名） */
    @PutMapping("/user/update")
    public Result<User> update(@RequestParam(required = false) String nickName,
                               @RequestParam(required = false) String signature,
                               HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        User user = userService.getById(userId);
        if (nickName != null) {
            user.setNickName(nickName);
        }
        if (signature != null) {
            user.setSignature(signature);
        }
        userService.updateById(user);
        return Result.success(user);
    }

    /**
     * PUT /update/password 修改密码。
     * 与原版语义对齐：旧密码错误返回 code=10006（我们测试原版时实测的错误码）。
     */
    @PutMapping("/update/password")
    public Result<String> updatePassword(@RequestParam String oldPassword,
                                         @RequestParam String newPassword,
                                         HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        User user = userService.getById(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            return Result.error(com.example.easychat.common.ResultCode.NOT_FRIEND); // 10006 旧密码错误
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userService.updateById(user);
        return Result.success("密码修改成功");
    }

    /** PUT /update/friend/remark 修改好友备注 */
    @PutMapping("/update/friend/remark")
    public Result<String> updateRemark(@RequestParam String friendId,
                                       @RequestParam String remark,
                                       HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        friendMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<UserFriend>()
                .eq(UserFriend::getUserId, userId)
                .eq(UserFriend::getFriendId, friendId)
                .set(UserFriend::getRemark, remark));
        return Result.success("备注已更新");
    }

    /** GET /fuzzy/query/users 按用户名/昵称模糊查（分页） */
    @GetMapping("/fuzzy/query/users")
    public Result<List<User>> fuzzyQueryUsers(@RequestParam String userName,
                                              @RequestParam(defaultValue = "1") int curPage,
                                              @RequestParam(defaultValue = "20") int pageSize) {
        // M6b 简化：不分页，like 查询（分页参数接收备用——与原版接口签名兼容）
        List<User> users = userService.list(new LambdaQueryWrapper<User>()
                .like(User::getUserName, userName)
                .or().like(User::getNickName, userName)
                .last("LIMIT " + Math.min(pageSize, 50)));
        return Result.success(users);
    }
}
