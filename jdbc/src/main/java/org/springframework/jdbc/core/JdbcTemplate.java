package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(String sql, Object...parameters) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setParameterIfExist(preparedStatement, parameters);
            log.info("JDBC EXECUTE SQL = {}", sql);
            preparedStatement.execute();
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... parameters) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setParameterIfExist(preparedStatement, parameters);
            log.info("JDBC QUERY SQL = {}", sql);
            return executeQuery(preparedStatement, new RowMapperResultSetExtractor<>(rowMapper));
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... parameters) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setParameterIfExist(preparedStatement, parameters);
            log.info("JDBC QUERY_FOR_OBJECT SQL = {}", sql);
            return getSingleResult(executeQuery(preparedStatement, new RowMapperResultSetExtractor<>(rowMapper)));
        }
    }

    private void setParameterIfExist(PreparedStatement preparedStatement, Object[] parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            preparedStatement.setObject(i + 1, parameters[i]);
        }
    }

    private <T> T executeQuery(PreparedStatement preparedStatement, ResultSetExtractor<T> resultSetExtractor)
            throws SQLException {
        ResultSet resultSet = preparedStatement.executeQuery();
        return resultSetExtractor.extractData(resultSet);
    }

    private <T> T getSingleResult(Collection<T> results) throws SQLException {
        if (results.isEmpty()) {
            throw new SQLException("일치하는 결과가 존재하지 않습니다.");
        }

        if (results.size() > 1) {
            throw new SQLException("결과가 2개 이상 존재합니다.");
        }

        return results.iterator().next();
    }
}
