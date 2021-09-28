create table if not exists users (
    id       bigint not null auto_increment,
    account  varchar(20),
    password varchar(20),
    email    varchar(20)
);

insert into users (account, password, email) values ('pomo', 'pomo', 'pomo@email.com');