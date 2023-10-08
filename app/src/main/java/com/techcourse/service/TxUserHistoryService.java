package com.techcourse.service;

import com.techcourse.domain.UserHistory;
import org.springframework.transaction.support.TransactionTemplate;

public class TxUserHistoryService implements UserHistoryService {

    private final UserHistoryService userHistoryService;

    public TxUserHistoryService(UserHistoryService userHistoryService) {
        this.userHistoryService = userHistoryService;
    }

    @Override
    public void insert(UserHistory userHistory) {
        TransactionTemplate.execute(() -> userHistoryService.insert(userHistory));
    }
}
