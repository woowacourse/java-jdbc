package com.interface21.jdbc.core;

import com.interface21.context.stereotype.Component;
import com.interface21.context.stereotype.Inject;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;

@Component
public class JdbcTemplate {

    @Inject
    private DataSource dataSource;

    private JdbcTemplate() {}

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, PreparedStatementSetter preparedStatementSetter) {
        execute(sql, preparedStatement -> {
            preparedStatementSetter.setValues(preparedStatement);
            return preparedStatement.executeUpdate();
        });
    }

    public void update(String sql, Object ... args) {
        update(sql, new ArgumentPreparedStatementSetter(args));
    }

    public <T> T query(String sql, ResultSetExtractor<T> resultSetExtractor, PreparedStatementSetter preparedStatementSetter) {
        return execute(sql, preparedStatement -> {
            preparedStatementSetter.setValues(preparedStatement);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSetExtractor.extract(resultSet);
        });
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object ... args) {
        return query(sql, new RowMapperResultSetExtractor<>(rowMapper), new ArgumentPreparedStatementSetter(args));
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object ... args) {
        return query(sql, new SingleResultSetExtractor<>(rowMapper), new ArgumentPreparedStatementSetter(args));
    }

    private <T> T execute(String sql, PreparedStatementExecutor<T> preparedStatementExecutor) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            return preparedStatementExecutor.execute(preparedStatement);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }
}
