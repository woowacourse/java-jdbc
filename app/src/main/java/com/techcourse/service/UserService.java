package com.techcourse.service;

import com.techcourse.domain.User;

public interface UserService {

    public User findById(final long id);

    public void insert(final User user);

    public void changePassword(final long id, final String newPassword, final String createBy);
}
