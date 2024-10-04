package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... args) {
        logDebug(sql);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; args != null && i < args.length; i++) {
                statement.setObject(i + 1, args[i]);
            }

            return statement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            return 0;
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, Object... args) {
        ArrayList<T> results = new ArrayList<>();
        logDebug(sql);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; args != null && i < args.length; i++) {
                statement.setObject(i + 1, args[i]);
            }

            ResultSet resultSet = statement.executeQuery();

            for (int rowNum = 0; resultSet.next(); rowNum++) {
                results.add(rowMapper.mapRow(resultSet, rowNum));
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }

        return Collections.unmodifiableList(results);
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, Object... args) {
        logDebug(sql);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; args != null && i < args.length; i++) {
                statement.setObject(i + 1, args[i]);
            }

            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                return Optional.empty();
            }

            return Optional.of(rowMapper.mapRow(resultSet, 0));
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            return Optional.empty();
        }
    }

    private void logDebug(String sql) {
        log.debug("query : {}", sql);
    }
}
