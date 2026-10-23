#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
M4 验收脚本：好友闭环（添加 -> 邀请推送 -> 接受 -> 双向落库）
被测对象：EasyChat-Rebuild（127.0.0.1:18001）
场景：alice 和 bob 两个真实用户、两条连接，完整走一遍好友闭环。
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
CMD_ADD_USER, CMD_ADD_USER_RESP = 7, 8
CMD_ADD_USER_SELF, CMD_ADD_USER_SELF_RESP = 9, 9   # 注意：自响应对本身无“请求”，只有 9
CMD_ACCEPT, CMD_ACCEPT_RESP = 19, 20


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
    s.sendall(enc(CMD_REGISTER, {"userName": user, "password": pwd, "nickName": nick,
                                  "login": False, "dateTime": time.strftime("%Y%m%d%H%M%S")}))
    return decode(s)


def login(s, user, pwd):
    s.sendall(enc(CMD_LOGIN, {"userName": user, "password": pwd, "sendCacheMessage": True,
                               "token": "", "dateTime": time.strftime("%Y%m%d%H%M%S")}))
    return decode(s)


results = []


def check(case, desc, ok, detail=""):
    results.append(ok)
    print(f"[{'PASS' if ok else 'FAIL'}] {case} {desc}" + (f"  | {detail}" if detail else ""))
    return ok


def main():
    stamp = str(int(time.time()))[-6:]
    alice, bob = f"alice{stamp}", f"bob{stamp}"
    pwd = "Friend@123"

    print(f"=== M4 验收：好友闭环 ===\n用户: {alice} / {bob}\n")

    sa = connect()
    sb = connect()

    # 注册并登录双方
    _, r = register(sa, alice, pwd, "爱丽丝")
    check("TC-M4-01", "注册 alice", r.get("success") is True)
    _, r = register(sb, bob, pwd, "鲍勃")
    check("TC-M4-02", "注册 bob", r.get("success") is True)

    _, ra = login(sa, alice, pwd)
    _, rb = login(sb, bob, pwd)
    check("TC-M4-03", "alice 登录", ra.get("success") is True)
    check("TC-M4-04", "bob 登录", rb.get("success") is True)
    alice_id, bob_id = ra.get("userId"), rb.get("userId")
    print(f"    alice userId={alice_id}, bob userId={bob_id}")

    # alice 添加 bob
    sa.sendall(enc(CMD_ADD_USER, {"addUserId": bob_id, "dateTime": time.strftime("%Y%m%d%H%M%S")}))

    # bob 应收到邀请（8）
    cmd, r = decode(sb)
    check("TC-M4-05", "bob 收到好友邀请(指令8)",
          cmd == 8 and r.get("inviterId") == alice_id,
          f"inviterId={r.get('inviterId')}")

    # alice 应收到自确认（9）
    cmd, r = decode(sa)
    check("TC-M4-06", "alice 收到自确认(指令9)",
          cmd == 9 and r.get("success") is True and r.get("reason") == "等待对方验证",
          f"reason={r.get('reason')}")

    # bob 同意（19）
    sb.sendall(enc(CMD_ACCEPT, {"receiver": alice_id, "accept": True,
                                 "dateTime": time.strftime("%Y%m%d%H%M%S")}))

    # 双方都应收到接受响应（20）
    cmd, r_accept_bob = decode(sb)
    check("TC-M4-07", "接受方 bob 收到响应(指令20)",
          cmd == 20 and r_accept_bob.get("success") is True,
          f"inviterId={r_accept_bob.get('inviterId')}, invitedId={r_accept_bob.get('invitedId')}")

    cmd, r_accept_alice = decode(sa)
    check("TC-M4-08", "邀请方 alice 收到响应(指令20)",
          cmd == 20 and r_accept_alice.get("success") is True
          and r_accept_alice.get("invitedId") == bob_id,
          f"invitedName={r_accept_alice.get('invitedName')}")

    # 幂等：alice 再添加 bob，应被拦截（已是好友）
    sa.sendall(enc(CMD_ADD_USER, {"addUserId": bob_id, "dateTime": time.strftime("%Y%m%d%H%M%S")}))
    cmd, r = decode(sa)
    check("TC-M4-09", "重复添加被幂等拦截", cmd == 9 and r.get("success") is False
          and "已经是你的好友" in str(r.get("reason")),
          f"reason={r.get('reason')}")

    sa.close()
    sb.close()

    passed = sum(1 for x in results if x)
    print(f"\n===== M4 验收: {passed}/{len(results)} 通过 =====")
    return 0 if passed == len(results) else 1


if __name__ == "__main__":
    sys.exit(main())
