drop table if exists klienci;
drop table if exists zamowienia;

begin;

create table klienci (
  idklienta integer primary key,
  nazwa varchar(50) not null,
  miejscowosc varchar(15),
  telefon varchar (20)
);

create table zamowienia (
  idzamowienia integer primary key,
  idklienta integer not null,
  opis varchar(100)
);

insert into klienci values
  (1, 'Hłasko Regina', 'Elbląg', '111 222 111'),
  (2, 'Pikowski Stefan', 'Kraków', '012 111 11 11'),
  (3, 'Czarnkowska Dalia', 'Iława', '111 222 001'),
  (4, 'Wandziak Wojciech', 'Warszawa', '111 222 002'),
  (7, 'Wojak Alicja', 'Wrocław', '111 222 003'),
  (8, 'Górka Andrzej', 'Gdańsk', '111 222 004'),
  (10, 'Moniak Antoni', 'Kraków', '012 222 22 00'),
  (11, 'Sokół Robert', 'Kraków', '012 111 11 00'),
  (12, 'Witak Nina', 'Warszawa', '022 888 88 00'),
  (13, 'Walendziak Jarosław', 'Warszawa', '022 888 88 01'),
  (14, 'Piotrowska Regina', 'Borki', '123 456 002'),
  (15, 'Miszak Stefan', 'Pomiechówek', '123 456 003');

insert into zamowienia values
  (1, 1, 'talon na balon'),
  (3, 2, 'opis3'),
  (4, 3, 'opis4'),
  (5, 4, 'placeholder'),
  (6, 5, 'blabla'),
  (7, 6, 'abc'),
  (8, 7, 'blabla'),
  (9, 8, 'abc'),
  (10, 9, 'blabla'),
  (11, 10, 'abc2'),
  (12, 11, 'blabla'),
  (13, 12, 'blabla'),
  (14, 13, 'abc3'),
  (15, 14, 'blabla');

commit;
