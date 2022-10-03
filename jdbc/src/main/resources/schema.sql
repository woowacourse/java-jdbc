create table if not exists member
(
    id       bigint auto_increment,
    name    varchar(255) not null,
    age     int not null,
    primary key (id)
);
