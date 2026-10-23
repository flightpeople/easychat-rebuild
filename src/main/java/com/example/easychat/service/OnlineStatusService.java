package com.example.easychat.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 在线状态服务（M6a）——Redis 的真实职责。
 *
 * 为什么在线状态放 Redis 而不是只用 SessionUtil 的内存 Map：
 * 1. /system/onlineNum 直接 SCARD 一个集合，O(1)，不用遍历 Map；
 * 2. 将来多实例部署（一个用户连在机器 A，查询打到机器 B）时，
 *    内存 Map 看不到别人的连接，Redis 是共享视图——
 *    这是“会话/状态外置”的标准起手式（内存 Map 是单机版，Redis 是集群版的地基）。
 *
 * 数据结构：Redis SET，key = easychat:online，member = userId。
 */
@Service
public class OnlineStatusService {

    private static final String ONLINE_KEY = "easychat:online";

    private final StringRedisTemplate redisTemplate;

    public OnlineStatusService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void online(String userId) {
        redisTemplate.opsForSet().add(ONLINE_KEY, userId);
    }

    public void offline(String userId) {
        redisTemplate.opsForSet().remove(ONLINE_KEY, userId);
    }

    public long count() {
        Long size = redisTemplate.opsForSet().size(ONLINE_KEY);
        return size == null ? 0 : size;
    }

    public Set<String> onlineUsers() {
        return redisTemplate.opsForSet().members(ONLINE_KEY);
    }
}
