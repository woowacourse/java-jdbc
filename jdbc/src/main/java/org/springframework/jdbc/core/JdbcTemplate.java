package org.springframework.jdbc.core;

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

    public void update(final String sql, final Object... args) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setPreparedStatement(preparedStatement, args);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setPreparedStatement(preparedStatement, args);
            List<T> results = extractData(rowMapper, preparedStatement);

            return results.iterator().next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            return extractData(rowMapper, preparedStatement);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void setPreparedStatement(final PreparedStatement ps, final Object[] args) throws SQLException {
        for (int i = 1; i <= args.length; i++) {
            ps.setObject(i, args[i - 1]);
        }
    }

    private <T> List<T> extractData(final RowMapper<T> rowMapper,
                                    final PreparedStatement preparedStatement) throws SQLException {
        try (final ResultSet resultSet = preparedStatement.executeQuery();) {
            List<T> results = new ArrayList<>();
            int rowNum = 0;
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet, rowNum++));
            }
            return results;
        }
    }
}
