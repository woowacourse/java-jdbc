package di.stage2.constructorwithinterfaces;

import di.User;

interface UserDao {

    void insert(User user);

    User findById(long id);
}
