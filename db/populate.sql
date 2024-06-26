insert into `show`(`name`, `country`) values ('What We Do in the Shadows', 'United States'), ('The Good Place', 'United States');
insert into `season`(`show`, `seasonNumber`) values (1, 1), (1, 2), (2, 1);
insert into `episode`(`season`, `episodeNumber`, `name`, `relDate`) values (1, 1, 'Pilot', '2019-03-28'), (1, 2, 'City Council', '2019-04-04');
insert into `actor`(`name`, `country`, `birthDate`) values ('Kayvan Novak', 'United Kingdom', '1978-11-23'), ('Kristen Bell', 'United States', '1980-07-18'), ('Kristen Schaal', 'United States', '1978-01-24');
insert into `main_cast` values (1, 1, 'Nandor The Relentless'), (2, 2, 'Eleanor Shellstrop');
insert into `featured_actor` values (3, 1, 'The Guide');