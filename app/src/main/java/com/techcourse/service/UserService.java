package com.techcourse.service;

import com.techcourse.domain.User;

public interface UserService {

    User findById(long id);

    void save(User user);

    void changePassword(long id, String newPassword, String createBy);
}
