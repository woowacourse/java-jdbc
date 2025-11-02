package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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

    public <T> T execute(String sql, PreparedStatementCallback<T> action, Object... args) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            log.debug("query : {}", sql);
            setParameters(preparedStatement, args);
            return action.doAction(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public void query(final String sql, final Object... args) {
        execute(sql, PreparedStatement::executeUpdate, args);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, preparedStatement -> {
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return rowMapper.mapRow(rs, 0);
                }
            }
            return null;
        }, args);
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper) {
        return execute(sql, preparedStatement -> {
            try (ResultSet rs = preparedStatement.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs, 0));
                }
                return results;
            }
        });
    }

    private void setParameters(final PreparedStatement preparedStatement, final Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }
    }
}
