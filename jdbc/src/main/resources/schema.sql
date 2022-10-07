drop table if exists users;

create table users
(
    id       bigint auto_increment,
    username varchar(100) not null,
    primary key (id)
);

insert into users (username) values ('test');
insert into users (username) values ('east');
