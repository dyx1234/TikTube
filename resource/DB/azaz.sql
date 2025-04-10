create table if not exists tb_collect
(
    id          bigint unsigned auto_increment comment '主键'
        primary key,
    follow_id   int unsigned                        not null comment '被关注的用户',
    user_id     int unsigned                        not null comment '操作关注用户',
    create_time timestamp default CURRENT_TIMESTAMP not null comment '创建时间'
)
    comment '用户收藏表' collate = utf8mb4_unicode_ci
                         row_format = DYNAMIC;

create table if not exists tb_comment
(
    id          bigint unsigned auto_increment comment '主键'
        primary key,
    video_id    bigint unsigned                            not null comment '被评论的视频',
    user_id     int unsigned                               not null comment '发表评论的用户',
    parent_id   bigint unsigned  default '0'               not null comment '关联的1级评论id，如果是一级评论，则值为0',
    answer_id   bigint unsigned  default '0'               not null comment '回复的评论id，如果是一级评论则为0',
    content     varchar(128) collate utf8mb4_general_ci    not null comment '评论的内容',
    liked_num   bigint unsigned                            null comment '点赞数',
    status      tinyint unsigned default '0'               null comment '0正常
            1删除不可见',
    create_time timestamp        default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time timestamp        default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '评论表' collate = utf8mb4_unicode_ci
                     row_format = DYNAMIC;


create table if not exists tb_follow
(
    id          bigint unsigned auto_increment comment '主键'
        primary key,
    follow_id   int unsigned                        not null comment '被关注的用户',
    user_id     int unsigned                        not null comment '操作关注用户',
    create_time timestamp default CURRENT_TIMESTAMP not null comment '创建时间'
)
    comment '用户关注表' collate = utf8mb4_unicode_ci
                         row_format = DYNAMIC;

create table if not exists tb_like
(
    id          bigint unsigned auto_increment comment '主键'
        primary key,
    author_id   int unsigned                        not null comment '发布视频的用户',
    video_id    bigint unsigned                     not null comment '主键',
    user_id     int unsigned                        not null comment '点赞的用户',
    create_time timestamp default CURRENT_TIMESTAMP not null comment '创建时间'
)
    comment '用户点赞表' collate = utf8mb4_unicode_ci
                         row_format = DYNAMIC;

create table if not exists tb_private_message
(
    id              bigint unsigned auto_increment comment '主键Id'
        primary key,
    sender_id       bigint unsigned                            not null comment '发送者id',
    receiver_id     bigint unsigned                            not null comment '接受者Id',
    message_type    tinyint unsigned default '0'               not null comment '消息类型,0：普通消息 1：朋友分享 2：系统消息',
    message_content varchar(128)                               not null comment '消息内容',
    status          tinyint unsigned default '0'               not null comment '消息状态：0正常    1删除',
    create_time     timestamp        default CURRENT_TIMESTAMP not null comment '发送时间'
)
    comment '私信信息表' collate = utf8mb4_unicode_ci
                         row_format = DYNAMIC;

create table if not exists tb_user
(
    id          int unsigned auto_increment comment '主键'
        primary key,
    phone       char(11)                                   not null comment '用户名',
    username    varchar(20)                                not null comment '用户名',
    password    varchar(32)                                not null comment '密码,md5加密',
    salt        char(5)                                    null comment '密码盐',
    image       varchar(255)                               null comment '头像',
    signature   varchar(64)                                null comment '个性签名',
    status      tinyint unsigned default '0'               null comment '0正常
            1锁定',
    create_time timestamp        default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time timestamp        default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    attention   int                                        null comment '关注数',
    followers   int                                        null comment '粉丝数',
    works       int                                        null comment '创作数',
    likes       int                                        null comment '获赞数',
    constraint phone
        unique (phone)
)
    comment '用户信息表' collate = utf8mb4_unicode_ci
                         row_format = DYNAMIC;

create table if not exists tb_video
(
    id          bigint unsigned auto_increment comment '主键'
        primary key,
    author_id   int unsigned                               not null comment '发布视频的用户',
    title       varchar(64)                                null comment '视频简介',
    section     tinyint unsigned default '0'               null comment '1-直播 2-体育 3-游戏 4-番剧 5-知识 6-娱乐 7-美食 8-时尚 9-热点',
    cover_url   varchar(255)                               not null comment '视频封面url',
    video_url   varchar(255)                               not null comment '视频文件url',
    status      tinyint unsigned default '0'               null comment '0正常
            1删除不可见',
    create_time timestamp        default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time timestamp        default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    likes       varchar(10)                                null comment '点赞总数',
    collects    varchar(10)                                null comment '收藏总数',
    comments    varchar(10)                                null comment '评论总数'
)
    comment '视频信息表' collate = utf8mb4_unicode_ci
                         row_format = DYNAMIC;

