package com.techcourse.repository;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;

import java.util.Optional;


public class UserDaoRepository {

    private final UserDao userDao;

    public UserDaoRepository() {
        userDao = new UserDao(DataSourceConfig.getInstance());
    }

    public void save(User user) {
        userDao.insert(user);
    }

    public Optional<User> findByAccount(String account) {
        return Optional.ofNullable(userDao.findByAccount(account));
    }
}

