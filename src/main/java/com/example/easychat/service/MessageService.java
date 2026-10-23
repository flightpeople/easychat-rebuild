package com.example.easychat.service;

import com.example.easychat.mapper.MessageMapper;
import com.example.easychat.model.Message;
import org.springframework.stereotype.Service;

/**
 * 消息服务（M5a）：消息落库。
 *
 * 为什么消息要先落库再投递：
 * 1. 对方不在线时，消息不能丢——上线后要能补发；
 * 2. 多端同步（手机+电脑）的根源就是服务端存着全量消息记录；
 * 3. 投递是“尽力而为”（写不进对方的连接就失败），落库是“必须成功”——
 *    先把必须成功的做了，再做尽力而为的。
 */
@Service
public class MessageService {

    private final MessageMapper messageMapper;

    public MessageService(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    public void save(String fromUserId, String toUserId, String content) {
        messageMapper.insert(new Message(fromUserId, toUserId, content));
    }
}
