package com.techcourse.dao;

import java.sql.Connection;

import com.techcourse.domain.UserHistory;

public interface UserHistoryDao {

    void log(final UserHistory userHistory);

    void log(Connection connection, UserHistory userHistory);
}
