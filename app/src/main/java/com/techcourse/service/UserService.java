package com.techcourse.service;

import com.techcourse.domain.User;

public interface UserService {

    User findById(final long id);
    void changePassword(final long id, final String newPassword, final String createdBy);
}
