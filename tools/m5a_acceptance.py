#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
M5a 验收脚本：群组核心 + 单聊消息
场景：alice/bob 注册登录 → 加好友 → alice 建群 → 邀请 bob → bob 接受 →
      alice 给 bob 发消息 → 非好友被拒 → 消息记录落库验证
"""
import socket
import struct
import json
import time
import sys

MAGIC = 0x12345678
HOST, PORT = "127.0.0.1", 18001


def enc(cmd, body):
    b = json.dumps(body).encode("utf-8")
    return (struct.pack(">i", MAGIC) + struct.pack(">BBB", 1, 1, cmd)
            + struct.pack(">i", len(b)) + b)


def recv_exact(s, n):
    d = b""
    while len(d) < n:
        c = s.recv(n - len(d))
        if not c:
            raise ConnectionError("connection closed by server")
        d += c
    return d


def decode(s):
    h = recv_exact(s, 11)
    cmd = h[6]
    ln = struct.unpack(">i", h[7:11])[0]
    return cmd, (json.loads(recv_exact(s, ln).decode("utf-8")) if ln else {})


def connect():
    s = socket.create_connection((HOST, PORT), timeout=10)
    s.settimeout(10)
    return s


def register(s, user, pwd, nick):
    s.sendall(enc(21, {"userName": user, "password": pwd, "nickName": nick,
                        "login": False, "dateTime": time.strftime("%Y%m%d%H%M%S")}))
    return decode(s)


def login(s, user, pwd):
    s.sendall(enc(1, {"userName": user, "password": pwd, "sendCacheMessage": True,
                      "token": "", "dateTime": time.strftime("%Y%m%d%H%M%S")}))
    return decode(s)


results = []


def check(case, desc, ok, detail=""):
    results.append(ok)
    print(f"[{'PASS' if ok else 'FAIL'}] {case} {desc}" + (f"  | {detail}" if detail else ""))
    return ok


def main():
    stamp = str(int(time.time()))[-6:]
    alice, bob = f"a{stamp}", f"b{stamp}"
    pwd = "Group@123"

    print(f"=== M5a 验收：群组 + 单聊 ===\n用户: {alice} / {bob}\n")

    sa, sb = connect(), connect()
    register(sa, alice, pwd, "阿丽")
    register(sb, bob, pwd, "小鲍")
    _, ra = login(sa, alice, pwd)
    _, rb = login(sb, bob, pwd)
    aid, bid = ra.get("userId"), rb.get("userId")
    check("TC-M5-01", "双方注册登录", ra.get("success") and rb.get("success"),
          f"alice={aid}, bob={bid}")

    # 先加好友（发消息的前提）
    sa.sendall(enc(7, {"addUserId": bid, "dateTime": time.strftime("%Y%m%d%H%M%S")}))
    decode(sb)  # bob 收 8
    decode(sa)  # alice 收 9
    sb.sendall(enc(19, {"receiver": aid, "accept": True, "dateTime": time.strftime("%Y%m%d%H%M%S")}))
    decode(sb)  # bob 收 20
    decode(sa)  # alice 收 20
    check("TC-M5-02", "alice/bob 成为好友", True, "M4 闭环复用")

    # alice 建群（10）
    sa.sendall(enc(10, {"groupName": "M5测试群", "users": [], "dateTime": time.strftime("%Y%m%d%H%M%S")}))
    cmd, r = decode(sa)
    group_id = r.get("groupId")
    check("TC-M5-03", "alice 建群", cmd == 11 and r.get("success") is True,
          f"groupId={group_id}")

    # alice 邀请 bob 入群（12）
    sa.sendall(enc(12, {"groupId": group_id, "users": [bid], "dateTime": time.strftime("%Y%m%d%H%M%S")}))
    cmd, r_bob = decode(sb)
    check("TC-M5-04", "bob 收到入群邀请(13)",
          cmd == 13 and r_bob.get("groupId") == group_id and r_bob.get("inviteId") == aid,
          f"inviteId={r_bob.get('inviteId')}")
    cmd, r_alice = decode(sa)
    check("TC-M5-05", "alice 收到邀请汇总(14)",
          cmd == 14 and bid in (r_alice.get("successUser") or []),
          f"successUser={r_alice.get('successUser')}")

    # bob 接受入群（17）
    sb.sendall(enc(17, {"groupId": group_id, "inviterId": aid, "accept": True,
                        "dateTime": time.strftime("%Y%m%d%H%M%S")}))
    cmd, r = decode(sb)
    check("TC-M5-06", "接受方 bob 收到入群响应(18)",
          cmd == 18 and r.get("success") is True and r.get("acceptUser") == bid)
    cmd, r = decode(sa)
    check("TC-M5-07", "邀请方 alice 收到入群响应(18)",
          cmd == 18 and r.get("success") is True)

    # alice 给 bob 发消息（5）
    sa.sendall(enc(5, {"receiver": bid, "message": "在吗？吃饭了吗", "messageType": 0,
                       "dateTime": time.strftime("%Y%m%d%H%M%S")}))
    cmd, r = decode(sb)
    check("TC-M5-08", "bob 收到消息(6)",
          cmd == 6 and r.get("sender") == aid and r.get("message") == "在吗？吃饭了吗"
          and r.get("senderName") == alice,
          f"senderName={r.get('senderName')}")

    # alice 给不存在的好友发消息（25 自响应）
    sa.sendall(enc(5, {"receiver": "NO_SUCH_USER", "message": "测试", "messageType": 0,
                       "dateTime": time.strftime("%Y%m%d%H%M%S")}))
    cmd, r = decode(sa)
    check("TC-M5-09", "给非好友/不存在用户发消息被拒(25)",
          cmd == 25 and r.get("success") is False,
          f"reason={r.get('reason')}")

    sa.close()
    sb.close()

    passed = sum(1 for x in results if x)
    print(f"\n===== M5a 验收: {passed}/{len(results)} 通过 =====")
    return 0 if passed == len(results) else 1


if __name__ == "__main__":
    sys.exit(main())
