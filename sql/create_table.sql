-- 用户中心表
create table user
(
    id           bigint auto_increment
        primary key,
    username     varchar(256)                       null,
    userAccount  varchar(256)                       null,
    avatarUrl    varchar(1024)                      null,
    gender       tinyint                            null,
    userPassword varchar(512)                       not null,
    phone        varchar(128)                       null,
    email        varchar(512)                       null,
    userStatus   int      default 0                 not null,
    createTime   datetime default CURRENT_TIMESTAMP null,
    updateTime   datetime default CURRENT_TIMESTAMP null,
    isDelete     tinyint  default 0                 not null,
    userRole     int      default 0                 not null comment '0-普通用户 1-管理员 2',
    planetCode   varchar(512)                       null comment '星球编号'
)
    comment '用户' engine = InnoDB;

alter table user add COLUMN tags varchar(1024) null comment;

-- auto-generated definition
# create table tags
# (
#     id         bigint auto_increment comment 'id'
#         primary key,
#     tagName    varchar(256)                       null comment '标签id',
#     userId     bigint                             null comment '用户id',
#     parentId   bigint                             null comment '父标签id',
#     isParent   tinyint                            null comment '0 - 不是 1 - 父标签 id',
#     createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
#     updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
#     isDelete   tinyint  default 0                 not null comment '是否删除',
#     constraint uniIdx_tagname
#         unique (tagName)
# )
#     comment '标签';
#
# create index idx_userId
#     on tags (userId);

-- 队伍表
create table team
(
    id         bigint auto_increment comment 'id'  primary key,
    name    varchar(256)                      not null comment '队伍名称',
    description varchar(1024)                      null comment '描述',
    maxNum     int                            default 1 not null comment '最大人数',
    expireTime datetime                         null     comment '过期时间',
    userId     bigint                             comment '用户id',
    status     int                                default 0 not null comment '0 - 公开， 1 - 私有. 2 - 加密',
    password   varchar(512)                            null comment '密码',

    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete   tinyint  default 0                 not null comment '是否删除'
) comment '队伍';

-- 用户队伍关系
create table team
(
    id         bigint auto_increment comment 'id'  primary key,
    userId     bigint                             comment '用户id',
    teamId     bigint                             comment '队伍id',
    joinTime   datetime                        null comment '加入时间',

    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete   tinyint  default 0                 not null comment '是否删除'
) comment '用户队伍关系';