package di.stage3.context;

import di.User;

interface UserDao {

    void insert(User user);

    User findById(long id);
}
