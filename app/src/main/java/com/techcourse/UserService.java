package com.techcourse;

import com.techcourse.domain.User;

public interface UserService {

    User findById(final long id);

    User findByAccount(String account);

    void save(final User user);

    void changePassword(long id, String newPassword, String createdBy);
}
