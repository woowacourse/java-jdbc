package com.techcourse.service;

import java.sql.Connection;
import java.sql.SQLException;
import org.springframework.jdbc.core.error.SqlExceptionConverter;

public abstract class TransactionService<T> {

    protected T appService;

    protected TransactionService(final T appService) {
        this.appService = appService;
    }

    protected void rollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw SqlExceptionConverter.convert(e);
        }
    }
}
