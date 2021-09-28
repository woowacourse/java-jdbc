create table if not exists users (
    id bigint auto_increment,
    account varchar(100) not null,
    password varchar(100) not null,
    email varchar(100) not null,
    UNIQUE (account),
    primary key(id)
);
