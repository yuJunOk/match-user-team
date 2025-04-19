create database db_multi

create table tb_tag
(
    id          bigint auto_increment comment 'id'
        primary key,
    tag_name    varchar(256)                       null comment '标签名',
    user_id     bigint                             null comment '用户id',
    parent_id   bigint                             null comment '父标签id',
    parent_flag tinyint                            null comment '是否父标签，1是，0否',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP null comment '更新时间',
    deleted     tinyint  default 0                 not null comment '是否删除',
    constraint uniIdx_tag_name
        unique (tag_name)
)
    comment '标签表';

create index idx_user_id
    on tb_tag (user_id);

create table tb_team
(
    id          bigint auto_increment comment 'id'
        primary key,
    name        varchar(256)                       not null comment '队伍名称',
    description varchar(1024)                      null comment '描述',
    max_num     int      default 1                 not null comment '最大人数',
    expire_time datetime                           null comment '过期时间',
    user_id     bigint                             null comment '用户id',
    status      int      default 0                 not null comment '0 - 公开，1 - 私有，2 - 加密',
    password    varchar(512)                       null comment '密码',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    deleted     tinyint  default 0                 not null comment '是否删除'
)
    comment '队伍';

create table tb_user
(
    id          bigint auto_increment comment 'id'
        primary key,
    user_name   varchar(256)                       null comment '用户名',
    avatar_url  varchar(1024)                      null comment '头像链接',
    gender      tinyint                            null comment '性别',
    tags        varchar(1024)                      null comment '标签列表',
    profile     varchar(512)                       null comment '用户简介',
    login_name  varchar(256)                       null comment '账号',
    login_pwd   varchar(512)                       not null comment '密码',
    phone       varchar(128)                       null comment '电话',
    email       varchar(512)                       null comment '邮箱',
    status      int      default 0                 not null comment '用户状态',
    user_role   int      default 0                 not null comment '用户角色 0为普通用户 1为管理员',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP null comment '更新时间',
    deleted     tinyint  default 0                 not null comment '是否删除'
)
    comment '用户表';

create table tb_user_team
(
    id          bigint auto_increment comment 'id'
        primary key,
    user_id     bigint                             null comment '用户id',
    team_id     bigint                             null comment '队伍id',
    join_time   datetime                           null comment '加入时间',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    deleted     tinyint  default 0                 not null comment '是否删除'
)
    comment '用户队伍关系';

