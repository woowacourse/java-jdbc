package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;
import com.techcourse.exception.UserNotFoundException;
import nextstep.web.annotation.Service;

@Service
public class UserService {

    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User findUserByAccount(String account) {
        return userDao.findByAccount(account)
            .orElseThrow(UserNotFoundException::new);
    }
}
