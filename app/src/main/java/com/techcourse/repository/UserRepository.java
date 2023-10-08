package com.techcourse.repository;

import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;

import java.sql.Connection;
import java.util.List;

public class UserRepository {

    private final UserDao userDao;

    public UserRepository(final UserDao userDao) {
        this.userDao = userDao;
    }

    public Integer save(final User user) {
        return userDao.insert(user);
    }

    public Integer update(final User user) {
        return userDao.update(user);
    }

    public List<User> findAll() {
        return userDao.findAll();
    }

    public User findById(final Long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("id가 " + id + "인 유저를 찾을 수 없습니다."));
    }

    public User findByAccount(final String account) {
        return userDao.findByAccount(account)
                .orElseThrow(() -> new IllegalArgumentException("account가 " + account + "인 유저를 찾을 수 없습니다."));
    }

}
