package com.techcourse.dao;

import com.techcourse.domain.UserHistory;
import javax.sql.DataSource;

public interface UserHistoryDao {

    void log(final UserHistory userHistory);

    DataSource getDataSource();
}
