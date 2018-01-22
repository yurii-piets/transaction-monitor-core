update studenci set nazwa='rollback failed' where wydzial='ieit';

insert into studenci values
         (20, 'Should Not Exist', 'human', 6.0),
         (21, 'Should Not Exist', 'human', 5.5);

delete from studenci where wydzial='eaiiib';

insert into studenci values
  ('EXCEPTION THROWN', 'EXCEPTION THROWN', 'EXCEPTION THROWN', 'EXCEPTION THROWN');
