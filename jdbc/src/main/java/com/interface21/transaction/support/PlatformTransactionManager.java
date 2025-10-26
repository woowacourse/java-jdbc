package com.interface21.transaction.support;

import java.sql.SQLException;

public interface PlatformTransactionManager {
    void getTransaction() throws SQLException;
    void commit() throws SQLException;
    void rollback();
}
