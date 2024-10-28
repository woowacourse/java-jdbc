package com.techcourse.service;

import com.techcourse.domain.User;

public interface UserService {

    void changePassword(final long id, final String newPassword, final String createBy);

    void insert(final User user);

    User findById(final long id);
}
