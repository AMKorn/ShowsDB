drop database if exists showsdb;

create database showsdb;

use showsdb;

create table `show` (
    `id` bigint primary key auto_increment,
    `name` varchar(255) not null,
    `releaseYear` int
);
insert into `show`(`name`, `releaseYear`) values ('What We Do in the Shadows', 2019);
insert into `show`(`name`, `releaseYear`) values ('The Good Place', 2017);

