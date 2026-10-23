#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
M6a 验收脚本：REST 查询通道 + 鉴权拦截器 + Redis 在线状态
流程：TCP 注册登录两人 → 加好友 → REST 三态鉴权(无token/假token/真token)
     → /users /user/{id} /groups /query/member → Redis 在线数核对 → 断线后计数归零
"""
import socket
import struct
import json
import time
import urllib.request
import urllib.error
import sys

MAGIC = 0x12345678
TCP_HOST, TCP_PORT = "127.0.0.1", 18001
REST = "http://127.0.0.1:19005"

OPENER = urllib.request.build_opener(urllib.request.ProxyHandler({}))


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


def tcp_connect():
    s = socket.create_connection((TCP_HOST, TCP_PORT), timeout=10)
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


def rest(method, path, token=None):
    req = urllib.request.Request(REST + path, method=method)
    if token:
        req.add_header("token", token)
    try:
        with OPENER.open(req, timeout=10) as resp:
            return resp.status, json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        try:
            return e.code, json.loads(e.read().decode("utf-8"))
        except Exception:
            return e.code, {}


results = []


def check(case, desc, ok, detail=""):
    results.append(ok)
    print(f"[{'PASS' if ok else 'FAIL'}] {case} {desc}" + (f"  | {detail}" if detail else ""))
    return ok


def main():
    stamp = str(int(time.time()))[-6:]
    alice, bob = f"ra{stamp}", f"rb{stamp}"
    pwd = "Rest@123"

    print(f"=== M6a 验收：REST 通道 + 鉴权 + Redis ===\n")

    sa, sb = tcp_connect(), tcp_connect()
    register(sa, alice, pwd, "丽丝")
    register(sb, bob, pwd, "鲍鲍")
    _, ra = login(sa, alice, pwd)
    _, rb = login(sb, bob, pwd)
    aid, bid = ra.get("userId"), rb.get("userId")
    token_a = ra.get("token")

    # 加好友（REST /users 要有数据）
    sa.sendall(enc(7, {"addUserId": bid, "dateTime": time.strftime("%Y%m%d%H%M%S")}))
    decode(sb); decode(sa)
    sb.sendall(enc(19, {"receiver": aid, "accept": True, "dateTime": time.strftime("%Y%m%d%H%M%S")}))
    decode(sb); decode(sa)

    # 建群 + bob 入群（REST /groups 要有数据）
    sa.sendall(enc(10, {"groupName": "REST测试群", "users": [], "dateTime": time.strftime("%Y%m%d%H%M%S")}))
    cmd, r = decode(sa)
    gid = r.get("groupId")
    sa.sendall(enc(12, {"groupId": gid, "users": [bid], "dateTime": time.strftime("%Y%m%d%H%M%S")}))
    decode(sb); decode(sa)
    sb.sendall(enc(17, {"groupId": gid, "inviterId": aid, "accept": True, "dateTime": time.strftime("%Y%m%d%H%M%S")}))
    decode(sb); decode(sa)
    check("TC-M6-01", "前置：好友+建群+bob入群", True, f"groupId={gid}")

    # 鉴权三态
    st, r = rest("GET", "/users")
    check("TC-M6-02", "无 token → 10003", r.get("code") == "10003", f"desc={r.get('desc')}")
    st, r = rest("GET", "/users", token="fake-token-xxx")
    check("TC-M6-03", "假 token → 10004", r.get("code") == "10004", f"desc={r.get('desc')}")

    # 真 token 查询
    st, r = rest("GET", "/users", token=token_a)
    check("TC-M6-04", "GET /users 好友列表",
          r.get("code") == "0" and len(r.get("data") or []) == 1
          and r["data"][0]["userId"] == bid,
          f"好友数={len(r.get('data') or [])}")

    st, r = rest("GET", f"/user/{bid}", token=token_a)
    check("TC-M6-05", "GET /user/{id} 查他人信息",
          r.get("code") == "0" and r["data"]["userName"] == bob,
          f"userName={r['data']['userName']}")

    st, r = rest("GET", "/groups", token=token_a)
    check("TC-M6-06", "GET /groups 我的群列表",
          r.get("code") == "0" and len(r.get("data") or []) == 1
          and r["data"][0]["groupName"] == "REST测试群",
          f"群数={len(r.get('data') or [])}")

    st, r = rest("GET", f"/query/member/{gid}", token=token_a)
    check("TC-M6-07", "GET /query/member/{gid} 群成员",
          r.get("code") == "0" and set(r.get("data") or []) == {aid, bid},
          f"成员={r.get('data')}")

    # 免鉴权端点
    st, r = rest("POST", "/system/onlineNum")
    check("TC-M6-08", "POST /system/onlineNum（免鉴权+Redis 计数）",
          r.get("code") == "0" and "2" in str(r.get("data")),
          f"data={r.get('data')}")

    st, r = rest("GET", "/system/checkVersion?versionCode=1")
    check("TC-M6-09", "GET /system/checkVersion（免鉴权）", r.get("code") == "0")

    # Redis 在线状态：断开 bob → 计数应变 1
    sb.close()
    time.sleep(1.5)
    st, r = rest("POST", "/system/onlineNum")
    check("TC-M6-10", "bob 断线后 Redis 在线数 -1",
          r.get("code") == "0" and "1" in str(r.get("data")),
          f"data={r.get('data')}")

    sa.close()

    passed = sum(1 for x in results if x)
    print(f"\n===== M6a 验收: {passed}/{len(results)} 通过 =====")
    return 0 if passed == len(results) else 1


if __name__ == "__main__":
    sys.exit(main())
