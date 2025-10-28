package com.techcourse.service;

import com.techcourse.domain.User;

public interface UserService {

    User getById(final long id);

    void save(final User user);

    void changePassword(final long id, final String newPassword, final String createdBy);
}
