create table if not exists users
(
    id       bigint auto_increment,
    username varchar(100) not null,
    primary key (id)
);
