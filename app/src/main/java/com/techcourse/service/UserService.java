package com.techcourse.service;

import com.techcourse.domain.User;

public interface UserService {

    User findById(final Long id);

    void insert(final User user);

    void changePassword(final Long id, final String newPassword, final String createBy);
}
