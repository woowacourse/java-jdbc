package com.techcourse.service;

import java.sql.SQLException;

@FunctionalInterface
public interface TransactionWork<T> {

    T execute() throws SQLException;
}
