package com.techcourse.dao;

import com.techcourse.domain.UserHistory;

public interface UserHistoryDao {

    void log(final UserHistory userHistory);
}
