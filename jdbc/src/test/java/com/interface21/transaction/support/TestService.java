package com.interface21.transaction.support;

public class TestService {

    private final TransactionManager transactionManager;

    public TestService(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    private void validMethod() {
        transactionManager.transaction(() -> {
        });
    }

    public void depthTwoValidMethod() {
        transactionManager.transaction(this::validMethod);
    }
}
