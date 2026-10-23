#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
M6b 验收脚本：REST 修改类 + 模糊查询 + 图片上传下载
"""
import socket
import struct
import json
import time
import base64
import urllib.request
import urllib.parse
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


def rest(method, path, token=None, body=None):
    data = None
    headers = {}
    if token:
        headers["token"] = token
    if body is not None:
        data = body if isinstance(body, bytes) else json.dumps(body).encode("utf-8")
        headers["Content-Type"] = "application/json"
    req = urllib.request.Request(REST + path, data=data, method=method, headers=headers)
    try:
        with OPENER.open(req, timeout=10) as resp:
            raw = resp.read()
            return resp.status, (json.loads(raw.decode("utf-8")) if raw else {})
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
    alice, bob = f"wa{stamp}", f"wb{stamp}"
    pwd = "Write@123"

    print(f"=== M6b 验收：REST 修改类 + 模糊查询 + 图片 ===\n")

    sa, sb = tcp_connect(), tcp_connect()
    register(sa, alice, pwd, "写操作甲")
    register(sb, bob, pwd, "写操作乙")
    _, ra = login(sa, alice, pwd)
    _, rb = login(sb, bob, pwd)
    aid, bid = ra.get("userId"), rb.get("userId")
    ta = ra.get("token")

    # 加好友（备注要有对象）
    sa.sendall(enc(7, {"addUserId": bid, "dateTime": time.strftime("%Y%m%d%H%M%S")}))
    decode(sb); decode(sa)
    sb.sendall(enc(19, {"receiver": aid, "accept": True, "dateTime": time.strftime("%Y%m%d%H%M%S")}))
    decode(sb); decode(sa)

    # 1. 改密码：旧密码错 → 10006
    st, r = rest("PUT", "/update/password?oldPassword=WRONG&newPassword=NewPass@456", token=ta)
    check("TC-M6b-01", "旧密码错误改密码 → 10006", r.get("code") == "10006", f"desc={r.get('desc')}")

    # 2. 改密码：正确
    st, r = rest("PUT", "/update/password?oldPassword=" + urllib.parse.quote(pwd)
                 + "&newPassword=NewPass@456", token=ta)
    check("TC-M6b-02", "正确改密码", r.get("code") == "0")
    # 新密码应能登录（旧连接已掉线没关系，重新连）
    sa.close()
    sa = tcp_connect()
    _, r = login(sa, alice, "NewPass@456")
    check("TC-M6b-03", "新密码可登录（旧密码失效）", r.get("success") is True)
    ta = r.get("token")

    # 3. 改资料（写控制器是 @RequestParam 风格）
    st, r = rest("PUT", "/user/update?nickName=" + urllib.parse.quote("改名成功")
                 + "&signature=" + urllib.parse.quote("这个人很懒"), token=ta)
    check("TC-M6b-04", "修改昵称签名", r.get("code") == "0"
          and r["data"]["nickName"] == "改名成功"
          and r["data"]["signature"] == "这个人很懒",
          f"nickName={r['data']['nickName']}")

    # 4. 好友备注
    st, r = rest("PUT", f"/update/friend/remark?friendId={bid}&remark=" + urllib.parse.quote("大老板"), token=ta)
    check("TC-M6b-05", "好友备注修改", r.get("code") == "0")

    # 5. 模糊查询用户
    st, r = rest("GET", f"/fuzzy/query/users?userName={bob[:4]}", token=ta)
    check("TC-M6b-06", "模糊查询用户", r.get("code") == "0"
          and any(u["userId"] == bid for u in (r.get("data") or [])),
          f"命中 {len(r.get('data') or [])} 人")

    # 6. 建群 + 改群（@RequestParam 风格）
    sa.sendall(enc(10, {"groupName": "改名测试群", "users": [], "dateTime": time.strftime("%Y%m%d%H%M%S")}))
    cmd, r = decode(sa)
    gid = r.get("groupId")
    st, r = rest("POST", "/group/update?groupId=" + gid
                 + "&groupName=" + urllib.parse.quote("已改名群"), token=ta)
    check("TC-M6b-07", "群成员改群名", r.get("code") == "0"
          and r["data"]["groupName"] == "已改名群", f"groupName={r['data']['groupName']}")

    # 7. 非群成员改群 → 10008
    st, r = rest("POST", "/group/update?groupId=" + gid
                 + "&groupName=" + urllib.parse.quote("蹭改"), token=rb.get("token"))
    check("TC-M6b-08", "非群成员改群被拒 → 10008", r.get("code") == "10008", f"desc={r.get('desc')}")

    # 8. 群模糊查询（原版此接口有 BUG-001，复刻版修复）
    st, r = rest("GET", "/fuzzy/query/groups?groupName=" + urllib.parse.quote("改名"), token=ta)
    check("TC-M6b-09", "群模糊查询（修复原版BUG-001）", r.get("code") == "0"
          and any(g["groupId"] == gid for g in (r.get("data") or [])),
          f"命中 {len(r.get('data') or [])} 个群")

    # 9. 图片上传（1x1 红色 PNG）
    png_b64 = ("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==")
    st, r = rest("POST", "/image/upload",
                 body={"picBase64": png_b64, "suffix": "png"})
    check("TC-M6b-10", "图片上传(免鉴权)", r.get("code") == "0" and r.get("data"),
          f"imageId={str(r.get('data'))[:12]}...")
    image_id = r.get("data")

    # 10. 图片下载（返回二进制，不走 JSON 解析）
    req = urllib.request.Request(REST + f"/image/download/{image_id}", method="GET")
    with OPENER.open(req, timeout=10) as resp:
        raw = resp.read()
    check("TC-M6b-11", "图片下载(免鉴权,PNG魔数校验)",
          resp.status == 200 and raw[:4] == b"\x89PNG",
          f"状态码={resp.status}, 大小={len(raw)}B")

    sa.close()
    sb.close()

    passed = sum(1 for x in results if x)
    print(f"\n===== M6b 验收: {passed}/{len(results)} 通过 =====")
    return 0 if passed == len(results) else 1


if __name__ == "__main__":
    sys.exit(main())
