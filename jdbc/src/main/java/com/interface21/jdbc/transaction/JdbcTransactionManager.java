package com.interface21.jdbc.transaction;

import com.interface21.transaction.core.AbstractTransactionManager;
import javax.sql.DataSource;

public class JdbcTransactionManager extends AbstractTransactionManager {

    public JdbcTransactionManager(final DataSource dataSource) {
        super(dataSource);
    }
}
