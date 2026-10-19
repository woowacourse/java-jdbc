create table if not exists users (
    id bigint auto_increment,
    account varchar(100) not null,
    password varchar(100) not null,
    email varchar(100) not null,
    primary key(id)
);

create unique index if not exists users_account_unique on users(account);

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
