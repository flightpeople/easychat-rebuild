package com.example.easychat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用入口（M3 起项目成为标准 Spring Boot 应用）。
 *
 * 启动顺序（关键设计）：Spring 上下文先起来（数据源、MyBatis、Service 就绪），
 * 再由 NettyStarter（CommandLineRunner）启动 Netty 服务端——
 * 保证 Netty 的 Handler 用到 UserService 时，数据库已经连好了。
 * 反过来（先起 Netty 后起 Spring）会出现“连接进来了但查不了库”的窗口期。
 */
@SpringBootApplication
@MapperScan("com.example.easychat.mapper")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
