#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
EasyChat TCP 协议客户端（测试用）
协议：4字节魔数 0x12345678 + 1字节版本 + 1字节序列化算法(1=JSON) + 1字节指令 + 4字节长度 + JSON体
"""
import socket
import struct
import json
import time
import sys

MAGIC = 0x12345678
VERSION = 1
ALGORITHM_JSON = 1

CMD_LOGIN = 1
CMD_LOGIN_RESP = 2
CMD_REGISTER = 21
CMD_REGISTER_RESP = 22
CMD_HEARTBEAT = 26
CMD_HEARTBEAT_RESP = 27

HOST = "127.0.0.1"
PORT = 18001


def encode_packet(command, body_dict):
    body = json.dumps(body_dict).encode("utf-8")
    return (struct.pack(">i", MAGIC)
            + struct.pack(">B", VERSION)
            + struct.pack(">B", ALGORITHM_JSON)
            + struct.pack(">B", command)
            + struct.pack(">i", len(body))
            + body)


def recv_exact(sock, n):
    data = b""
    while len(data) < n:
        chunk = sock.recv(n - len(data))
        if not chunk:
            raise ConnectionError("connection closed by server")
        data += chunk
    return data


def decode_packet(sock):
    header = recv_exact(sock, 11)
    magic, version, algorithm, command = struct.unpack(">iBBB", header[:7])
    length = struct.unpack(">i", header[7:11])[0]
    body = recv_exact(sock, length) if length > 0 else b""
    return command, (json.loads(body.decode("utf-8")) if body else {})


def now_str():
    return time.strftime("%Y%m%d%H%M%S")


def register(sock, username, password, nickname):
    body = {"userName": username, "password": password,
            "nickName": nickname, "login": False, "dateTime": now_str()}
    sock.sendall(encode_packet(CMD_REGISTER, body))
    return decode_packet(sock)


def login(sock, username, password):
    body = {"userName": username, "password": password,
            "sendCacheMessage": True, "token": "", "dateTime": now_str()}
    sock.sendall(encode_packet(CMD_LOGIN, body))
    return decode_packet(sock)


def heartbeat(sock):
    sock.sendall(encode_packet(CMD_HEARTBEAT, {"dateTime": now_str()}))
    return decode_packet(sock)


if __name__ == "__main__":
    username = sys.argv[1] if len(sys.argv) > 1 else "tester001"
    password = sys.argv[2] if len(sys.argv) > 2 else "Test@1234"

    sock = socket.create_connection((HOST, PORT), timeout=10)
    sock.settimeout(10)

    print(f"[1] 注册用户 {username} ...")
    cmd, resp = register(sock, username, password, "测试员001")
    print(f"    指令={cmd} 响应={json.dumps(resp, ensure_ascii=False)}")

    print(f"[2] 登录 {username} ...")
    cmd, resp = login(sock, username, password)
    print(f"    指令={cmd} 响应={json.dumps(resp, ensure_ascii=False)}")

    token = resp.get("token")
    if token:
        print(f"[3] TOKEN 获取成功: {token[:20]}...")
        with open("E:/houduan/easychat-server/tools/token.txt", "w", encoding="utf-8") as f:
            f.write(token)
        print("    token 已保存到 tools/token.txt")

        print("[4] 心跳测试 ...")
        cmd, resp = heartbeat(sock)
        print(f"    指令={cmd} 响应={json.dumps(resp, ensure_ascii=False)}")
    else:
        print("[!] 未获取到 token（可能用户已存在，改用登录）")

    sock.close()
