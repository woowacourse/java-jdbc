drop table if exists herb;

create table if not exists herb (
    id bigint auto_increment,
    name varchar(100) not null,
    age int not null,
    primary key(id)
);