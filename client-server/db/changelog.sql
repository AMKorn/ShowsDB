--liquibase formatted sql

--changeset andreas:1 labels:shows
--comment: creation of shows
create table `show` (
    `id` bigint primary key auto_increment,
    `name` varchar(255) not null,
    `country` varchar(255)
);
--rollback DROP TABLE `show`;

--changeset andreas:2 labels:shows
--comment: shows insertion
insert into `show`(`name`, `country`) values ('What We Do in the Shadows', 'United States');
insert into `show`(`name`, `country`) values ('The Good Place', 'United States');
--rollback DELETE FROM `show` WHERE `id` BETWEEN 1 AND 2

--changeset andreas:3 labels:seasons
--comment: season creation
create table `season` (
    `id` bigint primary key auto_increment,
    `show` int references `show`,
    `season_number` int not null,
    unique (`show`, `season_number`)
);
--rollback DROP TABLE `season`

--changeset andreas:4 labels:seasons
insert into `season`(`show`, `season_number`) values (1, 1), (1, 2), (2, 1);
--rollback DELETE FROM `season` where `id` between 1 and 3

--changeset andreas:5 labels:episodes
create table `episode` (
    `id` bigint primary key auto_increment,
    `season` bigint references `season`,
    `episode_number` int not null,
    `name` varchar(255),
    `rel_date` date,
	unique (`season`, `episode_number`)
);
--rollback drop table `episode`

--changeset andreas:6 labels:episodes
insert into `episode`(`season`, `episode_number`, `name`, `rel_date`) values (1, 1, 'Pilot', '2019-03-28'), (1, 2, 'City Council', '2019-04-04');
--rollback delete from `episodes` where `id` = 1 or `id`=2

--changeset andreas:7 labels:actors
create table `actor` (
    `id` bigint primary key auto_increment,
    `name` varchar(255) not null,
    `country` varchar(255),
    `birth_date` date
);
--rollback drop table `actor`

--changeset andreas:8 labels:actors
insert into `actor`(`name`, `country`, `birth_date`) values ('Kayvan Novak', 'United Kingdom', '1978-11-23'), ('Kristen Bell', 'United States', '1980-07-18'), ('Kristen Schaal', 'United States', '1978-01-24');
--rollback delete from `actor` where `id` between 1 and 3

--changeset andreas:9 labels:actors,shows
create table `main_cast` (
    `id_actor` bigint references `actor`,
    `id_show` bigint references `show`,
    `character` varchar(255),
    primary key (`id_actor`, `id_show`)
);
--rollback drop table `main_cast

--changeset andreas:10 labels:actors,shows
insert into `main_cast` values (1, 1, 'Nandor The Relentless'), (2, 2, 'Eleanor Shellstrop');
--rollback delete from `main_cast` where (`idActor`, `idShow`) = (1,1) or (`idActor`, `idShow`) = (2,2)

--changeset andreas:11 labels:actors,episodes
create table `featured_actor` (
    `id_actor` bigint references `actor`,
    `episode` int references `episode`,
    `character` varchar(255),
    primary key (`id_actor`, `episode`)
);
--rollback drop table `featured_actor`

--changeset andreas:12 labels:actors,episodes
insert into `featured_actor` values (3, 1, 'The Guide');
--rollback delete from `featured_actor` where (`idActor`, `episode`) = (3,1)