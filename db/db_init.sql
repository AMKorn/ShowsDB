drop database if exists `showsDB`;

create database `showsDB`;

use `showsDB`;

create table `show` (
    `id` bigint primary key auto_increment,
    `name` varchar(255) not null,
    `country` varchar(255)
);
insert into `show`(`name`, `country`) values ('What We Do in the Shadows', 'United States');
insert into `show`(`name`, `country`) values ('The Good Place', 'United States');

create table `season` (
    `id` bigint primary key auto_increment,
    `show` int references `show`,
    `seasonNumber` int not null,
    unique (`show`, `seasonNumber`)
);
insert into `season`(`show`, `seasonNumber`) values (1, 1), (1, 2), (2, 1);

create table `episode` (
    `id` bigint primary key auto_increment,
    `season` bigint references `season`,
    `episodeNumber` int not null,
    `name` varchar(255),
    `relDate` date,
	unique (`season`, `episodeNumber`)
);
insert into `episode`(`season`, `episodeNumber`, `name`, `relDate`) values (1, 1, 'Pilot', '2019-03-28'), (1, 2, 'City Council', '2019-04-04');

create table `actor` (
    `id` bigint primary key auto_increment,
    `name` varchar(255) not null,
    `country` varchar(255),
    `birthDate` date
);
insert into `actor`(`name`, `country`, `birthDate`) values ('Kayvan Novak', 'United Kingdom', '1978-11-23'), ('Kristen Bell', 'United States', '1980-07-18'), ('Kristen Schaal', 'United States', '1978-01-24');

create table `main_cast` (
    `idActor` bigint references `actor`,
    `idShow` bigint references `show`,
    `character` varchar(255),
    primary key (`idActor`, `idShow`)
);
insert into `main_cast` values (1, 1, 'Nandor The Relentless'), (2, 2, 'Eleanor Shellstrop');

create table `featured_actor` (
    `idActor` bigint references `actor`,
    `episode` int references `episode`,
    `character` varchar(255),
    primary key (`idActor`, `episode`)
);
insert into `featured_actor` values (3, 1, 'The Guide');
