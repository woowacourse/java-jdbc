package com.techcourse.service;

import com.techcourse.domain.User;
public interface UserService {
    User findById(long id);

    void insert(User user);

    void changePassword(long id, String newPassword, String createBy);
}
