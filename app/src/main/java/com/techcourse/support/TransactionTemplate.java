package com.techcourse.support;

import com.techcourse.config.DataSourceConfig;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class TransactionTemplate {

    public void execute(final TransactionCallBack callBack) {
        DataSource dataSource = DataSourceConfig.getInstance();
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            callBack.doInTransactionWithoutResult();
            connection.commit();
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new DataAccessException();
            }
            throw new DataAccessException();
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }
}
