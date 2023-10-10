package com.techcourse.service;

import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.UserHistory;

public class AppUserHistoryService implements UserHistoryService {

    private final UserHistoryDao userHistoryDao;

    public AppUserHistoryService(UserHistoryDao userHistoryDao) {
        this.userHistoryDao = userHistoryDao;
    }

    @Override
    public void insert(UserHistory userHistory) {
        userHistoryDao.insert(userHistory);
    }
}
