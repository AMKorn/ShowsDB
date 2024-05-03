--liquibase formatted sql

--changeset andreas:13 labels:users
--comment: creation of users
create table `user` (
    `id` bigint primary key auto_increment,
    `username` varchar(255) unique not null,
    `password` varchar(255)
);
--rollback DROP TABLE `user`;

--changeset andreas:14 labels:users
create table `role`(
    `id` bigint primary key auto_increment,
    `role` varchar(16) unique not null
)
-- rollback DROP TABLE `roles`;

--changeset andreas:15 labels:users
create table `user_role`(
    `user_id` bigint references `user`,
    `role` bigint references `role`,
    primary key (`user_id`,`role`)
)
--rollback DROP TABLE `user_role`;

--changeset andreas:16 labels:users
insert into `role`(`role`) values ('ADMIN'), ('USER')
--rollback TRUNCATE `role`;

--changeset andreas:17 labels:users
insert into `user`(`username`,`password`) values ('admin', '$2a$10$xSQPn9fPDmGB88ApFtjfu.8GbNt5NOoufyW3UN8p9yGJ4It0b/EiK'), ('user', '$2a$10$xSQPn9fPDmGB88ApFtjfu.8GbNt5NOoufyW3UN8p9yGJ4It0b/EiK');
--rollback TRUNCATE `user`;

--changeset andreas:18 labels:users
insert into `user_role` values (1, 1), (1, 2), (2, 2);
--rollback TRUNCATE `user_role`;