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
import org.springframework.jdbc.datasource.DataSourceUtils;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (final Connection connection = DataSourceUtils.getConnection(dataSource);
            final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            final Object[] parameters = args;
            for (int i = 0; i < parameters.length; i++) {
                pstmt.setObject(i + 1, parameters[i]);
            }

            final ResultSet resultSet = pstmt.executeQuery();
            final List<T> results = new ArrayList<>();
            if (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet, resultSet.getRow()));
            }
            return results;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (final Connection connection = DataSourceUtils.getConnection(dataSource);
            final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            final Object[] parameters = args;
            for (int i = 0; i < parameters.length; i++) {
                pstmt.setObject(i + 1, parameters[i]);
            }

            final ResultSet resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                return rowMapper.mapRow(resultSet, resultSet.getRow());
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int update(final String sql, final Object... args) {
        try (final Connection connection = DataSourceUtils.getConnection(dataSource);
            final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            final Object[] parameters = args;
            for (int i = 0; i < parameters.length; i++) {
                pstmt.setObject(i + 1, parameters[i]);
            }

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
