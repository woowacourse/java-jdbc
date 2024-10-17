package com.techcourse.service;

import com.techcourse.domain.User;

public interface UserService {

    void insert(final User user);

    void changePassword(long id, String newPassword, String createdBy);

    User getById(long id);
}
