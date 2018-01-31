update klienci set nazwa='Rollback Failed' where nazwa='Rollback Successful';

insert into klienci values
          (20, 'Successful Rollback Commit', 'PSQL', '404 404 404'),
          (21, 'Another Successful Commit', 'PSQL', '403 403 403'),
          (22, 'Yet Another Working Insert', 'PSQL', '402 402 402');

insert into zamowienia values
          (16, 20, 'Stop inserting!'),
          (17, 21, '9/11 was an inside job'),
          (18, 22, '¯\_(ツ)_/¯');

update klienci set idklienta='EXCEPTION THROWN';
