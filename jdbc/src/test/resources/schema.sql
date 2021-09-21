create table if not exists users (
    id bigint auto_increment,
    account varchar(100) not null,
    password varchar(100) not null,
    email varchar(100) not null,
    primary key(id)
);

insert into users (account, password, email) values ('junroot', 'rootzzang123', 'rootjjang@gmail.com');
insert into users (account, password, email) values ('junroot2', 'rootzzang1234', 'rootjjang2@gmail.com');
