package com.techcourse.service;

import java.sql.SQLException;

public interface UserService {

    void changePassword(final long id, final String newPassword, final String createBy) throws SQLException;
}
