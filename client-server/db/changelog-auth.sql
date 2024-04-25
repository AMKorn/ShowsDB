--liquibase formatted sql

--changeset andreas:13 labels:users
--comment: creation of users
create table `user` (
    `id` bigint primary key auto_increment,
    `username` varchar(255) unique not null,
    `password` varchar(255),
    `roles` varchar(255)
);
--rollback DROP TABLE `user`;