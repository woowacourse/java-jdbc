package com.techcourse.service;

import com.techcourse.domain.User;

public interface UserService {

    User findById(final long id);

    void insert(final User user);

    void changePassword(long id, String newPassword, String createBy);
}
