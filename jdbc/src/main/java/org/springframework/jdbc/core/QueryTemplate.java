package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class QueryTemplate {

    private static final Logger log = LoggerFactory.getLogger(QueryTemplate.class);

    private final DataSource dataSource;

    public QueryTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T update(String sql, UpdateExecutor<T> executor, Object... args) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = getInitializedPreparedStatement(sql, connection, args)) {
            return executor.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private PreparedStatement getInitializedPreparedStatement(String sql, Connection connection, Object... args)
            throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        initializePreparedStatementArguments(preparedStatement, args);

        return preparedStatement;
    }

    private void initializePreparedStatementArguments(PreparedStatement preparedStatement, Object... args)
            throws SQLException {
        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }
    }

    public <T> T query(String sql, SelectExecutor<T> executor, Object... args) {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = getInitializedPreparedStatement(sql, connection, args);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            return executor.execute(resultSet);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

}
