package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, @Nullable Object... args) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setPreparedStatement(preparedStatement, args);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void setPreparedStatement(final PreparedStatement ps, final Object[] args) throws SQLException {
        for (int i = 1; i <= args.length; i++) {
            ps.setObject(i, args[i - 1]);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, @Nullable Object... args) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setPreparedStatement(preparedStatement, args);
            final ResultSet resultSet = preparedStatement.executeQuery();
            List<T> results = extractData(rowMapper, resultSet);

            return results.iterator().next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            final ResultSet resultSet = preparedStatement.executeQuery();

            return extractData(rowMapper, resultSet);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> List<T> extractData(RowMapper<T> rowMapper, ResultSet resultSet) throws SQLException {
        List<T> results = new ArrayList<>();
        int rowNum = 0;
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet, rowNum++));
        }
        return results;
    }
}
