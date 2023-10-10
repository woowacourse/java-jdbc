package org.springframework.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(TransactionExecutor executor) {
        try {
            Connection connection = DataSourceUtils.getConnection(dataSource);

            executor.execute(connection);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

}
