drop database if exists showsdb;

create database showsdb;

use showsdb;

create table `show` (
    `id` bigint primary key auto_increment,
    `name` varchar(255) not null,
    `country` varchar(255),
    `releaseYear` int
);
insert into `show`(`name`, `releaseYear`) values ('What We Do in the Shadows', 2019);
insert into `show`(`name`, `releaseYear`) values ('The Good Place', 2017);

create table `season` (
    `id` bigint primary key auto_increment,
    `show` bigint references `show`,
    `seasonNumber` int not null
);
insert into `season`(`show`, `seasonNumber`) values (1, 1), (1, 2), (2, 1);

create table `episode` (
    `id` bigint primary key auto_increment,
    `season` bigint references `season`,
    `episodeNumber` int not null,
    `name` varchar(255),
    `relDate` date
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
    `idEpisode` bigint references `episode`,
    `character` varchar(255),
    primary key (`idActor`, `idEpisode`)
);
insert into `featured_actor` values (3, 1, 'The Guide');