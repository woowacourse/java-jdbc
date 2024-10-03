package di.stage4.annotations;

import di.User;

interface UserDao {

    void insert(User user);

    User findById(long id);
}
