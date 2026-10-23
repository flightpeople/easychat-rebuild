package com.example.easychat.config;

import com.example.easychat.common.Result;
import com.example.easychat.common.ResultCode;
import com.example.easychat.session.SessionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * REST 鉴权拦截器（M6a）——TCP 侧会话表的复用。
 *
 * 与原版的三段式校验完全一致（我们测试原版时实测过 10003/10004/10005）：
 *   1. 无 token 头 → 10003；
 *   2. token 在会话表查不到 → 10004 不合法；
 *   3. token 查得到但连接已关闭 → 10005 过期。
 *
 * 【双通道架构的精髓】REST 侧不自己维护一套登录态，
 * 直接复用 TCP 登录时建立的 SessionUtil——这就是原版
 * "登录走 TCP、查询走 REST、token 串起两个通道"的设计。
 *
 * 错误响应直接写 HttpServletResponse（不抛异常）——拦截器在
 * Controller 之前执行，抛异常要走全局异常处理，直接写更轻。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String uri = request.getRequestURI();
        // 免鉴权路径（与原版一致：系统接口、图片、swagger）
        if (uri.contains("system") || uri.contains("image") || uri.contains("swagger")) {
            return true;
        }

        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            writeError(response, ResultCode.NO_SESSION);
            return false;
        }
        Channel channel = SessionUtil.getChannel(token);
        if (channel == null) {
            writeError(response, ResultCode.INVALID_SESSION);
            return false;
        }
        if (!channel.isActive()) {
            writeError(response, ResultCode.EXP_SESSION);
            return false;
        }
        // 把 userId 挂到 request，Controller 直接取（当前登录用户）
        request.setAttribute("userId", SessionUtil.getUserId(channel));
        return true;
    }

    private void writeError(HttpServletResponse response, ResultCode rc) throws Exception {
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(rc)));
    }
}
