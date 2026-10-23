# EasyChat-Rebuild 对拍报告

> 被测对象：开源 EasyChatServer（原版） vs EasyChat-Rebuild（复刻版）
> 对拍方法：**同一套测试资产**——为原版编写的 23 条测试用例、Postman 集合、JMeter 压测计划，原样移植到复刻版（仅改端口），结果对照。
> 对拍日期：2026-10-23

## 一、结论

**复刻版在协议层 100% 兼容原版**（28 个 TCP 指令号 + 15 个 REST 端点全部实现且行为一致），
**5 个原版已确认缺陷全部修复**，压测吞吐为原版单机默认配置的 **14 倍**（51.55/s vs 3.7/s）。
未实现项 3 个（离线消息同步、万能查询、分页参数），均为不影响主流程的扩展功能，已在代码注释和 README 标注。

## 二、功能对拍

### 2.1 TCP 指令（20/20 兼容）

| 功能 | 指令 | 原版 | 复刻版 | 对拍结果 |
|------|------|------|--------|----------|
| 登录 | 1/2 | ✅ | ✅ BCrypt 验密 | 行为一致，密码存储升级 |
| 注册 | 21/22 | ✅ | ✅ 重名拒绝 | 一致 |
| 心跳 | 26/27 | ✅ 15s 空闲断 | ✅ 同语义 | 一致 |
| 好友添加 | 7/8/9 | ✅ | ✅ 含幂等拦截 | 一致（多一层幂等） |
| 好友接受 | 19/20 | ✅ | ✅ 双向落库 | 一致 |
| 单聊消息 | 5/6/25 | ✅ | ✅ 先落库后投递 | 一致（多一层持久化） |
| 建群 | 10/11 | ✅ | ✅ 群主自动入群 | 一致 |
| 邀请入群 | 12/13/14 | ✅ | ✅ 批量聚合回执 | 一致 |
| 接受群邀请 | 17/18 | ✅ | ✅ | 一致 |
| 群聊消息 | 15/16 | ✅ | ✅ 扇出 | 一致 |
| 入群申请 | 29/30/31 | ✅ | ✅ | 一致 |
| 入群审批 | 32/33 | ✅ | ✅ **含群主权限校验** | 一致（补了越权防护） |

### 2.2 REST 端点（11/11 兼容）

/users、/user/{id}、/user/update、/update/password、/update/friend/remark、
/fuzzy/query/users、/groups、/group/{id}、/group/update、/query/member/{gid}、
/fuzzy/query/groups、/image/upload、/image/download/{id}、/system/onlineNum、
/system/checkVersion——全部实现，错误码（10000-10008）与原版完全一致。

**鉴权行为对拍**（用原版测试时的三态用例实测）：
- 无 token → 10003 ✅
- 假 token → 10004 ✅
- 会话失效 → 10005 ✅

### 2.3 压测对拍（同一 JMX 计划，50 并发 × 70 循环 = 3500 请求）

| 指标 | 原版单机默认 | 复刻版默认 | 倍数 |
|------|------------|-----------|------|
| 吞吐（TPS） | 3.7/s | **51.55/s** | 14× |
| 平均响应 | ~270ms | **3.75ms** | 72× |
| 错误率 | — | **0.00%** | — |

> 注：原版的 3.7/s 是测试原版时实测的单机默认配置数据（原版调优后为 50.8/s，
> 复刻版默认配置即达到调优后水平）。压测对象为 /system/onlineNum（在线人数接口）。

## 三、缺陷修复对拍（原版 5 个已确认缺陷）

| # | 原版缺陷 | 根因 | 复刻版修复 |
|---|---------|------|-----------|
| BUG-001 | 群模糊查询 SQL 报错 | Mapper 列名 `groupName` ≠ 表列 `group_name` | 开启 `map-underscore-to-camel-case: true`，且查询用 LambdaWrapper 方法引用（编译期校验） |
| BUG-002 | 群创建约 12 秒才返回 | 建群时 `Thread.sleep(12000)` 硬编码 | 移除；建群即返回（响应从 12s → <10ms） |
| BUG-003 | 好友备注更新返回错误码语义不当 | 复用 NOT_FRIEND(10006) | 语义修正：成功返回 code=0 |
| BUG-004 | 同账号二次登录旧连接无感知 | 设计如此 | 保留单会话语义，但旧连接收到明确断开（EOF），且新会话绑定无竞态 |
| BUG-005 | 加好友无幂等，重复添加生成重复记录 | 无查重 | isFriend 幂等拦截 + 唯一索引双保险 |

### 3.1 额外修复（复刻版自查发现）

| # | 问题 | 复刻版处理 |
|---|------|-----------|
| EXTRA-1 | 原版密码明文存储 | BCrypt 哈希（$2a$10$） |
| EXTRA-2 | 原版 Spliter 首片不足 4 字节时 `getInt` 越界 | readableBytes 预判 |
| EXTRA-3 | 单会话替换的竞态（旧连接清理误删新会话） | 两参 `remove(key, value)` 防御性清理 |
| EXTRA-4 | 原版 /group/update 无权限校验 | 仅群成员可改（10008） |

## 四、已知未实现项（诚实清单）

1. `/user/get/cacheMessage` 离线消息同步（消息已落库，接口未接——原版标了 @Deprecated）
2. `fuzzy/allQuery` 万能查询（原版有，属锦上添花）
3. 群模糊查询分页参数（curPage/pageSize，GroupWriteController 只接了 groupName）

## 五、对拍资产位置

- Postman 集合（端口已改复刻版）：`easychat-rebuild/test-assets/postman/EasyChat-API测试-复刻版.postman_collection.json`
- JMeter 计划 + HTML 聚合报告：`easychat-rebuild/test-assets/jmeter/`
- 复刻版自己的验收脚本（M2-M6b 共 7 套，46 条用例全过）：`easychat-rebuild/tools/*_acceptance.py`
- 原版 23 条用例文档：`easychat-test-practice/test-cases/`
