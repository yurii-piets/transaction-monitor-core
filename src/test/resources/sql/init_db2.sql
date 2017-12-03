drop table studenci;
drop table oceny;

begin;

create table studenci (
  idstudenta serial primary key,
  nazwa varchar(50) not null,
  wydzial varchar(10) not null,
  wiek integer
);

create table oceny (
  idoceny serial primary key,
  idstudenta integer not null,
  przedmiot varchar(40) not null,
  ocena numeric(3,2) not null
);

insert into studenci values
      (1, 'Hłasko Regina', 'eaiiib', 22),
      (2, 'Pikowski Stefan', 'ieit', 23),
      (3, 'Czarnkowska Dalia', 'ieit', 21),
      (4, 'Wandziak Wojciech', 'eaiiib', 21),
      (7, 'Wojak Alicja', 'imir', 23),
      (8, 'Górka Andrzej', 'imir', 22),
      (10, 'Moniak Antoni', 'eaiiib', 25),
      (11, 'Sokół Robert', 'imir', 24),
      (12, 'Witak Nina', 'eaiiib', 20),
      (13, 'Walendziak Jarosław2','imir', 22),
      (14, 'Piotrowska Regina', 'eaiiib', 23),
      (15, 'Miszak Stefan', 'ieit', 24);

insert into oceny values
    (1, 1, 'Podstawy Elektroniki Cyfrowej', 2.0),
    (2, 4, 'Systemy Dynamiczne', 3.5),
    (3, 1, 'Fizyka', 3.0),
    (4, 1, 'Programowanie Obiektowe', 2.0),
    (5, 3, 'Podstawy Elektroniki Cyfrowej', 2.0),
    (6, 3, 'Systemy Dynamiczne', 5.0),
    (7, 2, 'Analiza Matematyczna', 2.0),
    (8, 4, 'Fizyka', 3.5),
    (9, 5, 'Podstawy Elektroniki Cyfrowej', 2.0),
    (10, 10, 'Programowanie Obiektowe', 4.5),
    (11, 11, 'Lingwistyka formalna i automaty', 3.5),
    (12, 13, 'Podstawy Elektroniki Cyfrowej', 2.0),
    (13, 14, 'Podstawy Elektroniki Cyfrowej', 2.0),
    (14, 15, 'Fizyka II', 3.5),
    (15, 8, 'Podstawy Elektroniki Cyfrowej', 2.0);

commit;
