drop database if exists showsdb;

create database showsdb;

use showsdb;

create table `show` (
    `id` bigint primary key auto_increment,
    `name` varchar(255) not null,
    `episodes` int
);
insert into `show`(`name`, `episodes`) values ("What We Do in the Shadows", 10);
insert into `show`(`name`, `episodes`) values ("The Good Place", 40);
