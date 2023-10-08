package com.techcourse.service;

import com.techcourse.domain.UserHistory;
import org.springframework.transaction.support.TransactionTemplate;

public class TxUserHistoryService implements UserHistoryService {

    private final TransactionTemplate transactionTemplate;
    private final UserHistoryService userHistoryService;

    public TxUserHistoryService(TransactionTemplate transactionTemplate, UserHistoryService userHistoryService) {
        this.transactionTemplate = transactionTemplate;
        this.userHistoryService = userHistoryService;
    }

    @Override
    public void insert(UserHistory userHistory) {
        transactionTemplate.execute(() -> userHistoryService.insert(userHistory));
    }
}
