package com.example.easychat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.easychat.model.User;

/**
 * 用户服务（M6b 起继承 MyBatis-Plus 的 IService，
 * 白嫖 updateById/list 等批量能力，不用自己写）。
 */
public interface UserService extends IService<User> {

    User register(String userName, String rawPassword, String nickName);

    User getByName(String userName);

    User getById(String userId);

    boolean existsByName(String userName);
}
