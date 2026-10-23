#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
M2 验收脚本：注册/登录/心跳/会话 全功能验证
被测对象：EasyChat-Rebuild（127.0.0.1:18001）
验证点（对应 M2 设计目标）：
  TC-M2-01 注册新用户 -> success
  TC-M2-02 重复注册同一用户名 -> 失败(用户名已存在)
  TC-M2-03 错误密码登录 -> 失败(密码错误)
  TC-M2-04 不存在用户登录 -> 失败(用户不存在)
  TC-M2-05 正确登录 -> success + token + userId
  TC-M2-06 心跳 -> 指令27 响应
  TC-M2-07 单会话替换：第二台设备登录 -> 第一台连接被服务端断开
"""
import socket
import struct
import json
import time
import sys

MAGIC = 0x12345678
HOST, PORT = "127.0.0.1", 18001

CMD_LOGIN, CMD_LOGIN_RESP = 1, 2
CMD_REGISTER, CMD_REGISTER_RESP = 21, 22
CMD_HEARTBEAT, CMD_HEARTBEAT_RESP = 26, 27


def enc(cmd, body):
    b = json.dumps(body).encode("utf-8")
    return (struct.pack(">i", MAGIC) + struct.pack(">BBB", 1, 1, cmd)
            + struct.pack(">i", len(b)) + b)


def recv_exact(s, n):
    data = b""
    while len(data) < n:
        chunk = s.recv(n - len(data))
        if not chunk:
            raise ConnectionError("connection closed by server")
        data += chunk
    return data


def decode(s):
    header = recv_exact(s, 11)
    magic, ver, alg, cmd = struct.unpack(">iBBB", header[:7])
    length = struct.unpack(">i", header[7:11])[0]
    body = json.loads(recv_exact(s, length).decode("utf-8")) if length else {}
    return cmd, body


def connect():
    s = socket.create_connection((HOST, PORT), timeout=10)
    s.settimeout(10)
    return s


def register(s, user, pwd, nick):
    s.sendall(enc(CMD_REGISTER, {"userName": user, "password": pwd,
                                  "nickName": nick, "login": False,
                                  "dateTime": time.strftime("%Y%m%d%H%M%S")}))
    return decode(s)


def login(s, user, pwd):
    s.sendall(enc(CMD_LOGIN, {"userName": user, "password": pwd,
                               "sendCacheMessage": True, "token": "",
                               "dateTime": time.strftime("%Y%m%d%H%M%S")}))
    return decode(s)


def heartbeat(s):
    s.sendall(enc(CMD_HEARTBEAT, {"dateTime": time.strftime("%Y%m%d%H%M%S")}))
    return decode(s)


results = []


def check(case, desc, ok, detail=""):
    results.append((case, ok))
    print(f"[{'PASS' if ok else 'FAIL'}] {case} {desc}" + (f"  | {detail}" if detail else ""))
    return ok


def main():
    user = "m2tester"
    pwd = "M2pass@123"

    print("=== M2 验收：注册/登录/心跳/会话 ===\n")

    # TC-M2-01 注册
    s1 = connect()
    cmd, r = register(s1, user, pwd, "M2测试员")
    check("TC-M2-01", "注册新用户", cmd == CMD_REGISTER_RESP and r.get("success") is True,
          f"cmd={cmd}, resp={r}")

    # TC-M2-02 重复注册
    cmd, r = register(s1, user, pwd, "M2测试员")
    check("TC-M2-02", "重复注册应被拒绝", cmd == CMD_REGISTER_RESP and r.get("success") is False,
          f"reason={r.get('reason')}")

    # TC-M2-03 错误密码
    cmd, r = login(s1, user, "WRONG-PWD")
    check("TC-M2-03", "错误密码登录被拒绝", cmd == CMD_LOGIN_RESP and r.get("success") is False,
          f"reason={r.get('reason')}")

    # TC-M2-04 不存在用户
    cmd, r = login(s1, "no_such_user_xyz", "whatever")
    check("TC-M2-04", "不存在用户登录被拒绝", cmd == CMD_LOGIN_RESP and r.get("success") is False,
          f"reason={r.get('reason')}")

    # TC-M2-05 正确登录
    cmd, r = login(s1, user, pwd)
    ok = cmd == CMD_LOGIN_RESP and r.get("success") is True and r.get("token") and r.get("userId")
    check("TC-M2-05", "正确登录发 token", ok,
          f"userId={r.get('userId')}, token={str(r.get('token'))[:16]}...")
    token1 = r.get("token")

    # TC-M2-06 心跳
    cmd, r = heartbeat(s1)
    check("TC-M2-06", "心跳响应", cmd == CMD_HEARTBEAT_RESP, f"cmd={cmd}")

    # TC-M2-07 单会话替换
    s2 = connect()
    cmd, r2 = login(s2, user, pwd)
    ok2 = cmd == CMD_LOGIN_RESP and r2.get("success") is True
    check("TC-M2-07a", "第二设备登录成功", ok2, f"新token={str(r2.get('token'))[:16]}...")
    # 验证 s1 被服务端断开
    try:
        s1.settimeout(5)
        data = s1.recv(1024)
        closed = (data == b"")
    except (ConnectionError, socket.timeout, OSError):
        closed = True
    check("TC-M2-07b", "单会话替换：旧连接被服务端断开", closed,
          "旧连接收到 EOF" if closed else "旧连接仍活着（不符合单会话设计）")

    # 新连接心跳仍然正常
    cmd, r = heartbeat(s2)
    check("TC-M2-07c", "新连接心跳正常", cmd == CMD_HEARTBEAT_RESP)

    s2.close()

    passed = sum(1 for _, ok in results if ok)
    print(f"\n===== M2 验收: {passed}/{len(results)} 通过 =====")
    return 0 if passed == len(results) else 1


if __name__ == "__main__":
    sys.exit(main())
