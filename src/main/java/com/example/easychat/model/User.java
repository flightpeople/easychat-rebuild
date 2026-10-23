package com.example.easychat.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 用户实体（M3：MySQL 持久化版）。
 *
 * MyBatis-Plus 注解：
 * - @TableName：实体类 -> 表名映射（不写默认按驼峰转下划线猜，这里显式指定）；
 * - @TableId：主键；userId 由程序生成（不是数据库自增），所以不配 AUTO——
 *   这是“分布式 ID 不用自增主键”的常见做法（自增主键在分库分表时是灾难）。
 *
 * 注意 passwordHash 不是 password：存 BCrypt 哈希，不存明文（改进点）。
 */
@TableName("sp_user")
public class User {

    @TableId(type = IdType.INPUT)
    private String userId;
    private String userName;
    private String passwordHash;
    private String nickName;
    private String avatar;
    private String signature;

    public User() {
    }

    public User(String userId, String userName, String passwordHash, String nickName) {
        this.userId = userId;
        this.userName = userName;
        this.passwordHash = passwordHash;
        this.nickName = nickName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
