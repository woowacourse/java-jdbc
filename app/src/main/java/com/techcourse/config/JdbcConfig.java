package com.techcourse.config;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.transaction.TransactionManager;

public class JdbcConfig {

    private static final TransactionManager TRANSACTION_MANAGER = new TransactionManager();
    private static final JdbcTemplate JDBC_TEMPLATE = new JdbcTemplate(DataSourceConfig.getInstance(), getTransactionManager());

    public static TransactionManager getTransactionManager() {
        return TRANSACTION_MANAGER;
    }

    public static JdbcTemplate getJdbcTemplate() {
        return JDBC_TEMPLATE;
    }
}
