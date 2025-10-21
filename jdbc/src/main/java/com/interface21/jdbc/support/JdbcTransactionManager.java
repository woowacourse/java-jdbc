package com.interface21.jdbc.support;

import com.interface21.transaction.support.AbstractTransactionManager;
import javax.sql.DataSource;

public class JdbcTransactionManager extends AbstractTransactionManager {

    public JdbcTransactionManager(final DataSource dataSource) {
        super(dataSource);
    }
}
