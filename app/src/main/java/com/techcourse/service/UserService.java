package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

public interface UserService {

    User findById(final long id);

    void insert(final User user);

    void changePassword(final long id, final String newPassword, final String createBy);
}
