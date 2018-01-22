insert into klienci values
                    (16, 'Pariusz Dalka', 'Krakow', '666 666 666'),
                    (23, 'Rollback Successful', 'PSQL', '010 001 100');

update klienci set nazwa='Lech Balcerowicz' where miejscowosc='Warszawa';
insert into zamowienia values(17, 14, 'insert successful');

update zamowienia set opis='update successful';
insert into zamowienia values (16, 15, 'insert successful');
