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
    `role` varchar(16) unique not null
)
-- rollback DROP TABLE `roles`;

--changeset andreas:15 labels:users
create table `user_role`(
    `user_id` bigint references `user`,
    `role` varchar(16) references `role`,
    primary key (`user_id`,`role`)
)
--rollback DROP TABLE `user_role`;

--changeset andreas:16 labels:users
insert into `role` values ('ADMIN'), ('USER')
--rollback TRUNCATE `role`;