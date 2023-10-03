create table if not exists members (
    id bigint auto_increment,
    nickname varchar(100) not null,
    primary key(id)
);
