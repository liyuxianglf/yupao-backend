--数据库初始化
create database if not exists yupao;
use yupao;

-- 用户表
CREATE TABLE `user`
(
    `username`     varchar(256)                                            DEFAULT '' COMMENT '用户昵称',
    `id`           bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `userAccount`  varchar(256)                                            DEFAULT NULL COMMENT '账号',
    `avatarUrl`    varchar(1024)                                           DEFAULT 'https://th.wallhaven.cc/small/zy/zygeko.jpg' COMMENT '用户头像',
    `gender`       tinyint(4) DEFAULT '2' COMMENT '性别 0-女 1-男 2-未填写',
    `userPassword` varchar(512) NOT NULL COMMENT '密码',
    `phone`        varchar(128)                                            DEFAULT '未填写' COMMENT '电话',
    `email`        varchar(512)                                            DEFAULT '未填写' COMMENT '邮箱',
    `userStatus`   int(11) NOT NULL DEFAULT '0' COMMENT '状态 0 - 正常',
    `createTime`   datetime                                                DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime`   datetime                                                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `isDelete`     tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除',
    `userRole`     int(11) NOT NULL DEFAULT '0' COMMENT '用户角色 0 - 普通用户 1 - 管理员',
    `profile`      varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT '未填写' COMMENT '人个介绍',
    `tags`         varchar(1024)                                           DEFAULT NULL COMMENT '标签 json 列表',
    `isNewUser`    tinyint(4) DEFAULT '0' COMMENT '是否是新用户 0-新用户 1-不是新用户',
    PRIMARY KEY (`id`)
)comment '用户';

-- 队伍表
CREATE TABLE `team`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `name`        varchar(256) NOT NULL COMMENT '队伍名称',
    `description` varchar(1024) DEFAULT NULL COMMENT '队伍描述',
    `maxNum`      int(11) NOT NULL DEFAULT '1' COMMENT '最大人数',
    `expireTime`  datetime      DEFAULT NULL COMMENT '过期时间',
    `userId`      bigint(20) DEFAULT NULL COMMENT '用户id（队长 id）',
    `status`      int(11) NOT NULL DEFAULT '0' COMMENT '0 - 公开，1 - 私有，2 - 加密',
    `password`    varchar(512)  DEFAULT NULL COMMENT '队伍密码',
    `createTime`  datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime`  datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `isDelete`    tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除',
    PRIMARY KEY (`id`)
)comment '队伍';

-- 用户队伍关系
create table user_team
(
    id         bigint auto_increment comment 'id'
        primary key,
    userId     bigint comment '用户id',
    teamId     bigint comment '队伍id',
    joinTime   datetime null comment '加入时间',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete   tinyint  default 0 not null comment '是否删除'
) comment '用户队伍关系';
