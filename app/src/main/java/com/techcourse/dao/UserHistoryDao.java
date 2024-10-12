package com.techcourse.dao;

import com.techcourse.domain.UserHistory;
import java.sql.Connection;

public interface UserHistoryDao {

    void log(final UserHistory userHistory);

    void log(final Connection conn, final UserHistory userHistory);
}
