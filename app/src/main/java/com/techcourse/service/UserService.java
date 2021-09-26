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

    public boolean login(String account, String password) {
        Optional<User> user = userDao.findByAccountAndPassoword(account, password);
        return user.isPresent();
    }

    public boolean register(User user) {
        try {
            userDao.insert(user);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    public Optional<User> findByAccount(String account) {
        return userDao.findByAccount(account);
    }
}
