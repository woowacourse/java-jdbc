create table if not exists member (
    id bigint auto_increment,
    username varchar(100) not null,
    primary key(id)
);

insert into member (username) values ('blackcat');
insert into member (username) values ('gugu');
insert into member (username) values ('gugu');
