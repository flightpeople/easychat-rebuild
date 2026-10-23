package com.example.easychat.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 消息记录实体（M5a）。
 *
 * 设计要点：先落库、后投递。消息是 IM 系统里最不能丢的数据——
 * 对方不在线时，库里存着，上线后可以补发（原版的 sendCacheMessage 机制就是干这个的）。
 * create_time 由数据库默认值生成，不用应用服务器的时间（多机部署时应用时钟不一定准）。
 */
@TableName("sp_message")
public class Message {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String fromUserId;
    private String toUserId;
    private String content;
    private Integer messageType;
    private LocalDateTime createTime;

    public Message() {
    }

    public Message(String fromUserId, String toUserId, String content) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.content = content;
        this.messageType = 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getMessageType() {
        return messageType;
    }

    public void setMessageType(Integer messageType) {
        this.messageType = messageType;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
