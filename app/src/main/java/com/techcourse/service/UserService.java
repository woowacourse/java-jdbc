package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;
import com.techcourse.exception.UserNotFoundException;
import nextstep.web.annotation.Autowired;
import nextstep.web.annotation.Service;

@Service
public class UserService {

    private final UserDao userDao;

    @Autowired
    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User findUserByAccount(String account) {
        return userDao.findByAccount(account)
            .orElseThrow(() -> new UserNotFoundException(String.format("%s 계정의 유저가 존재하지 않습니다.", account)));
    }
}
