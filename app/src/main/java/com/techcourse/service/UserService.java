package com.techcourse.service;

import com.techcourse.domain.User;

public interface UserService {

    void insert(final User user);

    void changePassword(final long id, final String newPassword, final String createBy);

    User findById(final long id);

    User findByAccount(String account);
}
