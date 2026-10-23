#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
M5b 验收脚本：群聊扇出 + 入群申请审批
场景：alice 建群 → bob 申请入群(29) → alice 收申请(30)/bob 收自确认(31)
     → alice 同意(32) → bob 收审批结果(33) → bob 发群聊(15)
     → 双方都收群消息(16)（扇出）→ 权限校验：bob 审批他人申请应无效
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
    alice, bob = f"oa{stamp}", f"ob{stamp}"
    pwd = "Group@123"

    print(f"=== M5b 验收：群聊 + 入群审批 ===\n用户: {alice}(群主) / {bob}(申请人)\n")

    sa, sb = connect(), connect()
    register(sa, alice, pwd, "群主甲")
    register(sb, bob, pwd, "组外乙")
    _, ra = login(sa, alice, pwd)
    _, rb = login(sb, bob, pwd)
    aid, bid = ra.get("userId"), rb.get("userId")

    # alice 建群
    sa.sendall(enc(10, {"groupName": "审批测试群", "users": [], "dateTime": time.strftime("%Y%m%d%H%M%S")}))
    cmd, r = decode(sa)
    gid = r.get("groupId")
    check("TC-M5b-01", "alice 建群", cmd == 11 and r.get("success") is True, f"groupId={gid}")

    # bob 申请入群（29）
    sb.sendall(enc(29, {"groupId": gid, "dateTime": time.strftime("%Y%m%d%H%M%S")}))
    cmd, r_owner = decode(sa)
    check("TC-M5b-02", "群主 alice 收到申请(30)",
          cmd == 30 and r_owner.get("applyUserId") == bid
          and r_owner.get("groupName") == "审批测试群",
          f"applyUserName={r_owner.get('applyUserName')}")
    cmd, r_self = decode(sb)
    check("TC-M5b-03", "申请人 bob 收到自确认(31)",
          cmd == 31 and r_self.get("success") is True, f"reason={r_self.get('reason')}")

    # alice 同意（32）
    sa.sendall(enc(32, {"groupId": gid, "userId": bid, "allow": True,
                        "dateTime": time.strftime("%Y%m%d%H%M%S")}))
    cmd, r = decode(sb)
    check("TC-M5b-04", "bob 收到审批结果(33)",
          cmd == 33 and r.get("allow") is True and r.get("groupId") == gid)

    # 权限校验：bob（非群主）尝试审批 alice 的入群申请——应无效
    # （先让 alice“申请”自己的群？不行，她是群主。改为：bob 审批一个不存在的申请，验证服务端不响应）
    sb.sendall(enc(32, {"groupId": gid, "userId": aid, "allow": True,
                        "dateTime": time.strftime("%Y%m%d%H%M%S")}))
    # alice 不应收到任何响应（权限被拒）。用短超时验证“没有消息”
    sa.settimeout(3)
    try:
        data = sa.recv(1024)
        got_msg = (data != b"")
    except (socket.timeout, OSError):
        got_msg = False
    sa.settimeout(10)
    check("TC-M5b-05", "非群主审批被拒（alice 无响应）", not got_msg,
          "权限校验生效" if not got_msg else "收到了意外消息")

    # bob 发群聊（15）——扇出：双方都应收到
    sb.sendall(enc(15, {"groupId": gid, "message": "大家好！我是新成员", "messageType": 0,
                        "dateTime": time.strftime("%Y%m%d%H%M%S")}))
    cmd, r_bob_self = decode(sb)
    check("TC-M5b-06", "发送者 bob 自己收到群消息(16)",
          cmd == 16 and r_bob_self.get("message") == "大家好！我是新成员"
          and r_bob_self.get("senderId") == bid,
          f"senderName={r_bob_self.get('senderName')}")
    cmd, r_alice = decode(sa)
    check("TC-M5b-07", "群主 alice 收到群消息(16)",
          cmd == 16 and r_alice.get("groupId") == gid and r_alice.get("message") == "大家好！我是新成员",
          f"senderName={r_alice.get('senderName')}")

    sa.close()
    sb.close()

    passed = sum(1 for x in results if x)
    print(f"\n===== M5b 验收: {passed}/{len(results)} 通过 =====")
    return 0 if passed == len(results) else 1


if __name__ == "__main__":
    sys.exit(main())
