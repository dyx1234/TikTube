# AZAZ数据库表结构设计文档

## AZAZ数据库表结构设计

### 1. 项目数据库表一览

| 表名               | 表含义     | 备注                         |
| ------------------ | ---------- | ---------------------------- |
| tb_user            | 用户表     | 保存系统用户信息             |
| tb_video           | 视频表     | 保存视频信息                 |
| tb_like            | 点赞关系表 | 保存用户和视频的点赞关系对应 |
| tb_collect         | 收藏关系表 | 保存用户和视频的收藏关系对应 |
| tb_follow          | 关注关系表 | 保存用户和用户的关注关系对应 |
| tb_comment         | 评论表     | 保存用户对视频或者评论的回复 |
| tb_private_message | 私信信息表 | 保存用户与用户之间的私信     |

### 2. 数据库表一览图

![数据库表一览图](E:\Code\Java\JavaWeb\AZAZ\resource\imgs\数据库表一览图.png)

### tb_user 用户信息表

用户表，保存用户的基本信息

| 字段名       | 类型                | 约束               | 备注                          |
| ------------ | ------------------- | ------------------ | ----------------------------- |
| id           | int unsigned        | 非空，自增         | 主键                          |
| phone        | char(11)            | 非空，唯一         | 手机号，登录凭证              |
| username     | varchar(20)         | 非空               | 用户名                        |
| password     | varchar(32)         | 非空               | 密码，md5加密存储             |
| salt         | char(5)             | 默认NULL           | 用于给密码加密时加盐，长度为5 |
| image        | varchar(255)        | 默认NULL           | 用户头像的url                 |
| signature    | varchar(64)         | 默认NULL           | 用户的个性签名                |
| status       | tinyint(1) unsigned | 默认正常           | 0正常    1锁定                |
| attention    | int                 | 默认0              | 用户关注的人数                |
| followers    | int                 | 默认0              | 用户的粉丝数                  |
| works        | int                 | 默认0              | 用户作品数                    |
| likes        | int                 | 默认0              | 用户获赞数                    |
| created_time | timestamp           | 非空，默认当前时间 | 注册时间                      |
| updated_time | timestamp           | 非空，默认当前时间 | 更新时间                      |

```SQL
DROP TABLE IF EXISTS `tb_user`;
CREATE TABLE `tb_user` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `phone` char(11) COLLATE utf8mb4_unicode_ci NOT NULL UNIQUE COMMENT '用户名', 
  `username` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码,md5加密',
  `salt` char(5) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '密码盐',
  `image` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像',
  `signature` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '个性签名', 
  `status` tinyint unsigned DEFAULT 0 COMMENT '0正常\r\n            1锁定',
  `attention` int DEFAULT  0 comment '关注数',
  `followers` int DEFAULT 0 comment '粉丝数',
  `works` int DEFAULT 0 comment '创作数',
  `likes` int DEFAULT 0 comment '获赞数',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='用户信息表';
```

### tb_video 视频信息表

保存视频信息

| 字段名       | 类型             | 约束               | 备注                             |
| ------------ | ---------------- | ------------------ | -------------------------------- |
| id           | bigint unsigned  | 非空，自增         | 主键                             |
| author_id    | int unsigned     | 非空               | 作者id                           |
| title        | varchar(64)      | 默认NULL           | 视频说明，展示于左下角(参考抖音) |
| section      | tinyint unsigned | 默认0              | 视频分类，0为热门，其他待定      |
| cover_url    | varchar(255)     | 非空               | 视频封面的url                    |
| video_url    | varchar(255)     | 非空               | 视频存储的url                    |
| likes        | int              | 默认0              | 视频被点赞数                     |
| collects     | int              | 默认0              | 视频被收藏数                     |
| comments     | int              | 默认0              | 视频评论数                       |
| status       | tinyint unsigned | 默认正常可见       | 视频状态。0正常    1删除         |
| created_time | timestamp        | 非空，默认当前时间 | 发布时间                         |
| updated_time | timestamp        | 非空，默认当前时间 | 更新时间                         |

```SQL
DROP TABLE IF EXISTS `tb_video`;
CREATE TABLE `tb_video` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `author_id` int unsigned NOT NULL COMMENT '发布视频的用户',
  `title` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '视频简介', 
  `section` tinyint unsigned DEFAULT 0 COMMENT '0热门分类\r\n  1待定',
  `cover_url` varchar(255) NOT NULL COLLATE utf8mb4_unicode_ci COMMENT '视频封面url',
  `video_url` varchar(255) NOT NULL COLLATE utf8mb4_unicode_ci COMMENT '视频文件url',
  `status` tinyint unsigned DEFAULT 0 COMMENT '0正常\r\n            1删除不可见',
  `likes` int DEFAULT 0 comment '点赞总数',
  `collects` int DEFAULT 0 comment '收藏总数',
  `comments` int DEFAULT 0 comment '评论总数',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='视频信息表';
```

### tb_like 点赞关系表

保存用户和视频的点赞关系对应

| 字段名       | 类型            | 约束               | 备注       |
| ------------ | --------------- | ------------------ | ---------- |
| id           | bigint unsigned | 非空，自增         | 主键       |
| author_id    | int unsigned    | 非空               | 作者id     |
| video_id     | bigint unsigned | 非空               | 视频id     |
| user_id      | int unsigned    | 非空               | 点赞的用户 |
| created_time | timestamp       | 非空，默认当前时间 | 点赞时间   |

```SQL
DROP TABLE IF EXISTS `tb_like`;
CREATE TABLE `tb_like` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `author_id` int unsigned NOT NULL COMMENT '发布视频的用户',
  `video_id` bigint unsigned NOT NULL COMMENT '被点赞的视频',
  `user_id` int unsigned NOT NULL COMMENT '点赞的用户', 
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='用户点赞表';
```

### tb_collect 收藏关系表

保存用户和视频的收藏关系对应

| 字段名       | 类型            | 约束               | 备注       |
| ------------ | --------------- | ------------------ | ---------- |
| id           | bigint unsigned | 非空，自增         | 主键       |
| author_id    | int unsigned    | 非空               | 作者id     |
| video_id     | bigint unsigned | 非空               | 视频id     |
| user_id      | int unsigned    | 非空               | 收藏的用户 |
| created_time | timestamp       | 非空，默认当前时间 | 收藏时间   |

```SQL
DROP TABLE IF EXISTS `tb_collect`;
CREATE TABLE `tb_collect` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `author_id` int unsigned NOT NULL COMMENT '发布视频的用户',
  `video_id` bigint unsigned NOT NULL COMMENT '被收藏的视频',
  `user_id` int unsigned NOT NULL COMMENT '收藏的用户', 
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='用户收藏表';
```

### tb_follow 关注关系表

保存用户和用户的关注关系对应

| 字段名       | 类型            | 约束               | 备注           |
| ------------ | --------------- | ------------------ | -------------- |
| id           | bigint unsigned | 非空，自增         | 主键           |
| follow_id    | int unsigned    | 非空               | 被关注的用户   |
| user_id      | int unsigned    | 非空               | 操作关注的用户 |
| created_time | timestamp       | 非空，默认当前时间 | 点赞时间       |

```SQL
DROP TABLE IF EXISTS `tb_follow `;
CREATE TABLE `tb_follow ` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `follow_id` int unsigned NOT NULL COMMENT '被关注的用户',
  `user_id` int unsigned NOT NULL COMMENT '操作关注用户', 
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='用户关注表';
```

### tb_comment 评论表

保存用户对视频和评论的评论回复信息

| 字段名       | 类型             | 约束               | 备注                                     |
| ------------ | ---------------- | ------------------ | ---------------------------------------- |
| id           | bigint unsigned  | 非空，自增         | 主键                                     |
| video_id     | bigint unsigned  | 非空               | 视频id                                   |
| user_id      | int unsigned     | 非空               | 发表评论的用户                           |
| parent_id    | bigint unsigned  | 默认0              | 关联的1级评论id，如果是一级评论，则值为0 |
| answer_id    | bigint unsigned  | 默认0              | 回复的评论id，如果回复的视频则为0        |
| content      | varchar(255)     | 非空               | 评论内容                                 |
| liked_num    | bigint unsigned  | 点赞数             | 默认是0                                  |
| status       | tinyint unsigned | 默认正常可见       | 评论状态。0正常    1删除                 |
| created_time | timestamp        | 非空，默认当前时间 | 发布时间                                 |
| updated_time | timestamp        | 非空，默认当前时间 | 更新时间                                 |

```SQL
DROP TABLE IF EXISTS `tb_comment`;
CREATE TABLE `tb_comment` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `video_id` bigint unsigned NOT NULL COMMENT '被评论的视频',
  `user_id` int unsigned NOT NULL COMMENT '发表评论的用户', 
  `parent_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '关联的1级评论id，如果是一级评论，则值为0',
  `answer_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '回复的评论id，如果是一级评论则为0',
  `content` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '评论的内容',
  `liked_num` bigint UNSIGNED NULL DEFAULT NULL COMMENT '点赞数',
  `status` tinyint unsigned DEFAULT 0 COMMENT '0正常\r\n            1删除不可见',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='评论表';
```

### tb_private_message 私信信息表

用户之间可以互相私信，保存私信信息

| 字段名          | 类型             | 约束               | 备注                                 |
| --------------- | ---------------- | ------------------ | ------------------------------------ |
| id              | bigint unsigned  | 非空，自增         | 主键                                 |
| sender_id       | bigint unsigned  | 非空               | 发送消息的用户                       |
| receiver_id     | bigint unsigned  | 非空               | 接收消息的用户                       |
| message_type    | tinyint unsigned | 默认0              | 0 为私信 1 为朋友分享消息 2 系统消息 |
| message_content | varchar(128)     | 非空               | 消息内容                             |
| status          | tinyint unsigned | 默认正常可见       | 消息状态。0正常    1删除             |
| created_time    | timestamp        | 非空，默认当前时间 | 发送时间                             |

```SQL
CREATE TABLE `tb_private_message` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键Id',
  `sender_id` bigint unsigned NOT NULL COMMENT '发送者id',
  `receiver_id` bigint unsigned NOT NULL COMMENT '接受者Id',
  `message_type` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '消息类型,0：普通消息 1：朋友分享 2：系统消息',
  `message_content` varchar(128) NOT NULL COMMENT '消息内容',
  `status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '消息状态：0正常    1删除',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY  (`id`)
)  ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='私信信息表';
```