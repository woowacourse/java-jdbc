package com.interface21.jdbc.core;


import com.interface21.jdbc.exception.EmptyResultDataAccessException;
import com.interface21.jdbc.exception.IncorrectResultSizeDataAccessException;
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

    public void update(String sql, Object... columns) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            for (int i = 1; i <= columns.length; i++) {
                preparedStatement.setObject(i, columns[i - 1]);
            }

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            List<T> result = new ArrayList<>();
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                log.debug("query : {}", sql);

                while (resultSet.next()) {
                    result.add(rowMapper.map(resultSet));
                }
                return result;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... columns) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            for (int i = 1; i <= columns.length; i++) {
                preparedStatement.setObject(i, columns[i - 1]);
            }
            List<T> result = new ArrayList<>();
            try (ResultSet rs = preparedStatement.executeQuery()) {
                log.debug("query : {}", sql);

                while (rs.next()) {
                    result.add(rowMapper.map(rs));
                }
            }
            if (result.isEmpty()) {
                throw new EmptyResultDataAccessException();
            }
            if (result.size() != 1) {
                throw new IncorrectResultSizeDataAccessException();
            }
            return result.getFirst();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
