insert into oceny values (16, 8, 'Programownie Obiektowe', 4.5);

update oceny set ocena=2.0 where przedmiot like 'Fizyka%';

delete from studenci where wydzial='imir';

update studenci set nazwa='update successful' where wydzial='ieit';
