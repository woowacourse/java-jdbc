package org.springframework.jdbc.transaction;

import java.sql.Connection;

public interface TransactionExecutor<T> {

    T execute(final Connection connection);

}
