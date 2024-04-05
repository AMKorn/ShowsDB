drop database if exists showsdb;

create database showsdb;

use showsdb;

create table `show` (
    `id` bigint primary key auto_increment,
    `name` varchar(255) not null,
    `episodes` int
);

create table `actor` (
    `id` bigint primary key auto_increment,
    `name` varchar(255) not null,
    `birthDate` date
);