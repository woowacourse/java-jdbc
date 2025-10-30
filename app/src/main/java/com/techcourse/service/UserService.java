package com.techcourse.service;

import com.interface21.transaction.Transactional;
import com.techcourse.domain.User;

public interface UserService {

    User findById(long id);

    void insert(User user);

    @Transactional
    void changePassword(long id, String newPassword, String createBy);
}
