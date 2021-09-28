package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;
import java.util.Optional;
import nextstep.web.annotation.Service;

@Service
public class UserService {

    private UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public Optional<User> loginAccount(String account, String password) {
        Optional<User> user = userDao.findByAccountAndPassoword(account, password);
        return user;
    }

    public boolean isAvailableRegisteredAccount(User user) {
        Optional<User> existedUser = userDao.findByAccount(user.getAccount());
        if (existedUser.isPresent()) {
            return false;
        }
        userDao.insert(user);
        return true;
    }

    public Optional<User> findByAccount(String account) {
        return userDao.findByAccount(account);
    }
}
