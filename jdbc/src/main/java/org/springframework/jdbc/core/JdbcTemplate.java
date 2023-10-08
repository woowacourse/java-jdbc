package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... parameters) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = getPreparedStatement(sql, connection, parameters)) {
            log.debug("query : {}", sql);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = getPreparedStatement(sql, connection, parameters);
             final ResultSet resultSet = preparedStatement.executeQuery()) {
            log.debug("query : {}", sql);
            final List<T> rsult = new ArrayList<>();
            while (resultSet.next()) {
                rsult.add(rowMapper.mapRow(resultSet));
            }
            return rsult;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        final List<T> result = query(sql, rowMapper, parameters);
        if (result.size() > 1) {
            throw new DataAccessException("예상하는 쿼리의 결과보다 많이 결과가 나왔습니다.");
        }
        return Optional.ofNullable(result.get(0));
    }

    private PreparedStatement getPreparedStatement(
            final String sql,
            final Connection connection,
            final Object... parameters
    ) throws SQLException {
        final PreparedStatement preparedStatement = connection.prepareStatement(sql);
        for (int index = 0; index < parameters.length; index++) {
            preparedStatement.setObject(index + 1, parameters[index]);
        }
        return preparedStatement;
    }
}
