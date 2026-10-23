-- EasyChat-Rebuild 数据库结构（M3：用户表；M4/M5 会追加好友/群/消息表）
-- 命名与原版对齐（sp_ 前缀），便于 M7 测试套件对拍时对照

CREATE DATABASE IF NOT EXISTS easychat_rebuild DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;
USE easychat_rebuild;

DROP TABLE IF EXISTS sp_user;
CREATE TABLE sp_user (
    user_id       VARCHAR(32)  NOT NULL COMMENT '用户ID（10位随机字母数字）',
    user_name     VARCHAR(64)  NOT NULL COMMENT '账号（登录名，唯一）',
    password_hash VARCHAR(100) NOT NULL COMMENT 'BCrypt密码哈希（非明文，改进点）',
    nick_name     VARCHAR(64)  DEFAULT NULL COMMENT '昵称',
    avatar        VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
    signature     VARCHAR(255) DEFAULT NULL COMMENT '个性签名',
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_user_name (user_name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- ----------------------------
-- M4：好友关系表
-- ----------------------------
USE easychat_rebuild;
DROP TABLE IF EXISTS sp_user_friend;
CREATE TABLE sp_user_friend (
    id        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '自增主键（内部关系表，无需对外暴露）',
    user_id   VARCHAR(32)  NOT NULL COMMENT '关系所有者',
    friend_id VARCHAR(32)  NOT NULL COMMENT '好友ID',
    remark    VARCHAR(64)  DEFAULT NULL COMMENT '好友备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_friend (user_id, friend_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '好友关系表（双向冗余存储）';

-- ----------------------------
-- M5a：群组 + 成员 + 消息
-- ----------------------------
USE easychat_rebuild;

DROP TABLE IF EXISTS sp_group;
CREATE TABLE sp_group (
    group_id     VARCHAR(32)  NOT NULL COMMENT '群ID（随机串，同 user_id 策略）',
    group_name   VARCHAR(64)  NOT NULL COMMENT '群名',
    main_user_id VARCHAR(32)  NOT NULL COMMENT '群主',
    avatar       VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (group_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '群组表';

DROP TABLE IF EXISTS sp_user_group;
CREATE TABLE sp_user_group (
    id       BIGINT      NOT NULL AUTO_INCREMENT,
    group_id VARCHAR(32) NOT NULL,
    user_id  VARCHAR(32) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_group_user (group_id, user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '群成员表';

DROP TABLE IF EXISTS sp_message;
CREATE TABLE sp_message (
    id           BIGINT   NOT NULL AUTO_INCREMENT,
    from_user_id VARCHAR(32) NOT NULL COMMENT '发送者',
    to_user_id   VARCHAR(32) NOT NULL COMMENT '接收者（单聊=userId，群聊=groupId）',
    content      TEXT     NOT NULL COMMENT '消息内容',
    message_type INT      NOT NULL DEFAULT 0 COMMENT '0=文本',
    create_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_receiver (to_user_id, id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '消息记录表（先落库后投递，保证不丢）';
