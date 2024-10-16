package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.IncorrectParameterCountException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlExecutor {

    private static final Logger log = LoggerFactory.getLogger(SqlExecutor.class);

    public <T> T execute(
            String sql, DataSource dataSource, PreparedStatementExecutor<T> statementExecutor, Object... parameters) {
        log.debug("실행 쿼리: {}", sql);
        log.debug("파라미터: {}", Arrays.toString(parameters));
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            setParameters(preparedStatement, parameters);
            return statementExecutor.execute(preparedStatement);

        } catch (SQLException e) {
            log.error("쿼리 실행에 실패했습니다: {}", sql, e);
            throw new DataAccessException("쿼리 실행에 실패했습니다.", e);
        } finally {
            DataSourceUtils.releaseJdbcConnection(connection, dataSource);
        }
    }

    private void setParameters(PreparedStatement preparedStatement, Object... parameters) throws SQLException {
        validateParameterCount(preparedStatement, parameters);
        for (int i = 0; i < parameters.length; i++) {
            preparedStatement.setObject(i + 1, parameters[i]);
        }
    }

    private void validateParameterCount(PreparedStatement preparedStatement, Object... parameters) throws SQLException {
        ParameterMetaData parameterMetaData = preparedStatement.getParameterMetaData();
        int expectedParameterCount = parameterMetaData.getParameterCount();
        if (expectedParameterCount != parameters.length) {
            throw new IncorrectParameterCountException(expectedParameterCount, parameters.length);
        }
    }
}
