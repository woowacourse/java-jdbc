package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private static void setUpPreparedStatement(PreparedStatement prepareStatement, Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            prepareStatement.setObject(i + 1, args[i]);
        }
    }

    public void update(String sql, Object... args) {
        try (
            Connection conn = dataSource.getConnection();
            PreparedStatement prepareStatement = conn.prepareStatement(sql)
        ) {
            setUpPreparedStatement(prepareStatement, args);
            prepareStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        try (
            Connection conn = dataSource.getConnection();
            PreparedStatement prepareStatement = conn.prepareStatement(sql)
        ) {
            setUpPreparedStatement(prepareStatement, args);
            ResultSet resultSet = prepareStatement.executeQuery();
            if (resultSet.next()) {
                return rowMapper.mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
