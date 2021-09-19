package com.techcourse.dao;

import java.sql.SQLException;

public class UserDaoException extends RuntimeException{

    public UserDaoException(final SQLException e) {
        super(e.getMessage());
    }
}
