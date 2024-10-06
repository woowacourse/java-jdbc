create table if not exists test (
    id bigint auto_increment,
    content varchar(100) not null,
    primary key(id)
);

DELETE FROM test;
