# match-user-team
实现标签用户推荐匹配和队伍组建加入功能的移动端 H5 网站(APP 风格)，基于 Spring Boot后端+Vue3 前端的 全栈项目，包括用户登录、更新个人信息、按标签搜索用户、建房组队、推荐相似用户等功能。

# 一、技术栈

前端：Vue3 + Vant UI + Vite脚手架 + Axios + qs

后端：SpringBoot3 + MySQL + MyBatis + MyBatis-plus + MyBatis X 自动生成 + Redis + Redisson + Spring Scheduler + Hutool + Swagger + 字符串相似度算法

# 二、功能大纲

1. 用户的CRUD、登录注册、根据标签匹配推荐用户
2. 队伍的创建、加入、退出和解散

这个该项目基本覆盖了企业开发中常见的需求以及对应的解决方案，比如登录注册、信息检索展示、Swagger生成接口调试网页、定时任务、资源抢占等。并且涵盖了分布式、并发编程、锁、事务、缓存、性能优化、幂等性、数据一致性、大数据、算法等后端程序员必须了解的知识与实践。

# 三、项目截图

1. 主页用户推荐

![主页用户推荐](/doc/images/主页用户推荐.png)





2. 队伍列表页面

![队伍列表页面](/doc/images/队伍列表页面.png)

3. 我的信息

![我的信息](/doc/images/我的信息.png)

4. 创建编辑队伍

![创建编辑队伍](/doc/images/创建编辑队伍.png)

4. Swagger接口页面

![swagger接口页面](/doc/images/swagger接口页面.png)
