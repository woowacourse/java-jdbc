create table if not exists crew
(
    id       bigint auto_increment,
    nickname    varchar(255) not null,
    name        varchar(255) not null,
    age     int not null,
    primary key (id)
);
