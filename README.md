# EasyChat-Rebuild

协议兼容的 IM 后端复刻项目。目标：用全新实现复刻开源 EasyChatServer 的功能与协议
（同魔数 `0x12345678`、同指令表、同帧格式、同 REST 端点），
使已有的测试套件（23 条用例 + Postman + JMeter）可直接用于验收。

## 当前进度

```
M1 协议层 + Netty 骨架        ✅ 已完成（登录链路验证通过）
M2 注册/登录/心跳/会话        ✅ 已完成（9/9 验收通过）
M3 MySQL 真实存储             ✅ 已完成（9/9 + 重启持久化验证通过）
M4 好友闭环                   ✅ 已完成（9/9 + 双向落库验证通过）
M5a 群组 + 单聊消息           ✅ 已完成（9/9 + 三表落库验证通过）
M5b 群聊扇出 + 入群审批       ✅ 已完成（7/7 + 权限校验通过）
M6a REST 查询通道 + Redis     ✅ 已完成（10/10 + 断线计数验证通过）
M6b REST 修改类 + 图片        ✅ 已完成（11/11 全过）
M7 测试套件对拍 + 文档         ✅ 已完成（对拍报告见 docs/对拍报告.md）
```

**项目收官状态：全部模块完成。** 对拍结论：协议 100% 兼容（28 个 TCP 指令 + 15 个 REST 端点），
原版 5 个缺陷全修复，压测吞吐 51.55/s（原版单机默认 3.7/s 的 14 倍）。

## 运行端口（双通道）

| 通道 | 端口 | 职责 |
|------|------|------|
| Netty TCP | 18001 | 实时通道：登录/消息/心跳/推送 |
| Spring MVC HTTP | 19005 | 查询通道：好友列表/群列表/用户信息（token 鉴权复用 TCP 会话） |

## 指令实现清单（对照原版协议文档）

| 功能 | 指令 | 状态 |
|------|------|------|
| 登录 / 注册 | 1/2, 21/22 | ✅ |
| 心跳保活 | 26/27 | ✅ |
| 好友添加闭环 | 7/8/9, 19/20 | ✅ |
| 单聊消息 | 5/6, 25 | ✅ |
| 建群 | 10/11 | ✅ |
| 邀请入群 | 12/13, 14 | ✅ |
| 接受群邀请 | 17/18 | ✅ |
| 群聊消息（扇出） | 15/16 | ✅ |
| 入群申请审批 | 29/30/31, 32/33 | ✅ |

## 技术栈

Java 17 + Spring Boot 3.2 + Netty 4.1.108 + MyBatis-Plus 3.5.7 + MySQL 8.4 +
BCrypt + HikariCP + Maven（M6 起加入 Redis）

## 如何运行

前置：MySQL 已建库（`easychat_rebuild`，表结构见 `src/main/resources/sql/schema.sql`，
root/123456，按 application.yml 配置修改）。

```bash
# 启动（日常重启，2 秒级）——双击 start.bat，或：
cd E:\houduan\easychat-rebuild
"C:\Program Files\Java\jdk-21.0.12.1\bin\java.exe" -jar target\easychat-rebuild-1.0.0.jar

# 改了代码才需要重新打包（约 1 分钟，这是编译不是启动）：
E:\houduan\dev-tools\apache-maven-3.9.9\bin\mvn -s E:\houduan\dev-tools\settings.xml clean package -DskipTests
```

> 历史教训：早期用 `java -cp` 裸跑 + 每次重启前 `mvn dependency:build-classpath`，
> 把“重启”搞成了分钟级。打 fat jar（spring-boot-maven-plugin）后，
> 启动 = `java -jar` 一条命令，Spring Boot 自报启动耗时约 1.6-2 秒。

## 如何验证（M2 验收）

服务启动后另开一个终端：

```bash
python E:\houduan\easychat-rebuild\tools\m2_acceptance.py
```

9 条验收用例：注册 / 重名拒绝 / 错密码 / 不存在用户 / 正常登录发 token /
心跳 / 单会话替换（第二设备登录踢掉第一台）。

## 协议速查

帧格式：`魔数4B(0x12345678) | 版本1B(1) | 序列化算法1B(1=JSON) | 指令1B | 长度4B | JSON体`

已实现指令：登录(1/2)、注册(21/22)、心跳(26/27)。
待实现：好友(7/8/9/19/20)、群组(10-18/29-33)、消息(5/6/15/16/25)。

## 目录结构

```
src/main/java/com/example/easychat/
├── Application.java      # Spring Boot 入口（M3 起）
├── protocol/             # 协议常量与 Packet 基类（魔数/指令表/序列化算法）
├── codec/                # Spliter 拆包 + PacketCodec 编解码 + PacketRegistry 指令注册表
├── packet/               # 各指令的数据包（login/register/heartbeat/friend，M5 扩充）
├── session/              # SessionUtil 会话表（token↔Channel，单会话管理）
├── mapper/               # MyBatis-Plus Mapper（UserMapper/FriendMapper）
├── service/              # UserService/MyBatisUserService/FriendService
├── model/                # User/UserFriend 实体
├── handler/              # 一包一处理器：Register/Login/Heartbeat/AddUser/AcceptReq
└── server/               # NettyServer + NettyStarter（Spring 就绪后启动 Netty）
tools/
├── m2_acceptance.py      # M2 验收脚本（9 例）
├── m4_acceptance.py      # M4 验收脚本（9 例，双用户好友闭环）
└── tcp_client_18001.py   # 协议客户端（端口 18001）
```

## 开发踩坑记录（面试谈资）

1. `LengthFieldBasedFrameDecoder` 的三参 `decode` 是 final 不能覆盖，
   要覆盖单参 `decode(ctx, in)`——读原版源码 + javap 验证才确认；
2. 共享 Handler 实例必须标 `@Sharable`，否则 `ChannelPipelineException`；
3. `spring-security-crypto` 的 commons-logging 是 optional 传递依赖，
   单独使用时必须显式声明，否则运行到 `new BCryptPasswordEncoder()` 才炸
   （M3 引入 Spring Boot 后由 spring-jcl 自动满足，该显式依赖已移除）；
4. `mvn clean` 会删掉 `dependency:build-classpath` 生成的 cp.txt，重启前要重新生成；
5. 原版 Spliter 有 `getInt` 越界隐患（首片不足 4 字节时），复刻版加了 readableBytes 判断；
6. TCP `recv(n)` 不保证一次返回 n 字节——客户端和服务端都要循环收满（长度字段的意义）；
7. **`spring-boot-starter-parent` 只管版本不管引入**：基础 starter 不含 jackson-databind，
   必须显式声明依赖（版本号可省）——"版本管理"和"依赖引入"是两回事；
8. TaskStop 杀掉的 java 子进程可能变孤儿继续占端口，重启前必须 netstat 确认端口已释放；
9. **Spring Boot 配置优先级（实测）**：命令行 `-D` > 环境变量 > application.yml。
   从后台/脚本启动时，环境里继承的 `SERVER__PORT` 会静默覆盖 yml 里的端口
   （Tomcat 因此绑到随机端口 65436 撞车）——可靠做法是启动命令显式带
   `-Dserver.port=19005`；
10. **辅助依赖不该阻塞主流程**：登录路径里的 Redis 在线状态调用，在 Redis 挂掉时
    会拖垮登录（Lettuce 默认 60s 命令超时）。生产应对辅助依赖做 try-catch 降级——
    这是本次开发暴露的韧性设计问题，已记录待改进。
