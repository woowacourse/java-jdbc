package org.springframework.jdbc.core;

import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class QueryExecutorService {

    private final DataSource dataSource;

    public QueryExecutorService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(QueryExecutor<T> queryExecutor, String sql, Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = makePreparedStatement(connection, sql, args)) {

            return queryExecutor.execute(preparedStatement);
        } catch (SQLException e) {
            throw new DataAccessException();
        }
    }

    private PreparedStatement makePreparedStatement(Connection connection, String sql, Object[] args) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }

        return preparedStatement;
    }


}
