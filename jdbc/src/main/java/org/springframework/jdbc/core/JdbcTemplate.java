package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... args) {
        try(final Connection conn = dataSource.getConnection();
            final PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setAllArguments(args, preparedStatement);
            preparedStatement.executeUpdate();
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setAllArguments(final Object[] args, final PreparedStatement preparedStatement) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }
    }

    @Nullable
    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        log.debug("query : {}", sql);
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setAllArguments(args, preparedStatement);
            final ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                final T result = rowMapper.mapRow(resultSet);
                resultSet.close();
                return result;
            }

            resultSet.close();
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public DataSource dataSource() {
        return dataSource;
    }
}
