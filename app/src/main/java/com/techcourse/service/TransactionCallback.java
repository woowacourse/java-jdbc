package com.techcourse.service;

import java.sql.SQLException;

@FunctionalInterface
public interface TransactionCallback<T> {

    T doInTransaction() throws SQLException;
}
