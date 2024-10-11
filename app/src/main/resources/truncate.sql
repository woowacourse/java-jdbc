delete from users;
delete from user_history;

truncate table users restart identity;
truncate table user_histroy restart identity;
