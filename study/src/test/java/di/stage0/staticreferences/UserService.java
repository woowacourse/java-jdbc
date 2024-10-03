package di.stage0.staticreferences;

import di.User;

class UserService {

    public static User join(User user) {
        UserDao.insert(user);
        return UserDao.findById(user.getId());
    }
}
