create table if not exists users (
    id bigint auto_increment,
    account varchar(100) not null,
    password varchar(100) not null,
    email varchar(100) not null,
    primary key(id)
);

create table if not exists user_history (
    id bigint auto_increment,
    user_id bigint not null,
    account varchar(100) not null,
    password varchar(100) not null,
    email varchar(100) not null,
    created_at datetime not null,
    created_by varchar(100) not null,
    primary key(id)
);

truncate table users;
truncate table user_history;

alter table users alter column id restart with 1;
alter table user_history alter column id restart with 1;
