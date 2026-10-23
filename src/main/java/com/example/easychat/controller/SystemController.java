package com.example.easychat.controller;

import com.example.easychat.common.Result;
import com.example.easychat.service.OnlineStatusService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统 Controller（M6a）：免鉴权端点。
 *
 * onlineNum 现在查 Redis（M5 时代是遍历内存 Map）——同一份数据两个视角：
 * Map 服务“转发时找人”，Redis 服务“全局计数”，都从登录/断线事件同步。
 */
@RestController
public class SystemController {

    private final OnlineStatusService onlineStatusService;

    public SystemController(OnlineStatusService onlineStatusService) {
        this.onlineStatusService = onlineStatusService;
    }

    @PostMapping("/system/onlineNum")
    public Result<String> onlineNum() {
        return Result.success("系统在线人数为：" + onlineStatusService.count());
    }

    @GetMapping("/system/checkVersion")
    public Result<String> checkVersion(@RequestParam Integer versionCode) {
        // M6a 简化：无版本表，固定返回“已是最新”
        return Result.success("当前已是最新版本");
    }
}
