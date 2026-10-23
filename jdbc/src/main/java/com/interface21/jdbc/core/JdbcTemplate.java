package com.interface21.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object[] parameters) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setParameters(statement, parameters);
            log.debug("query : {}", sql);
            return statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("SQL 실행에 실패했습니다.", exception);
        }
    }

    public List<Object> query(final String sql, final Object[] parameters, final RowMapper rowMapper) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setParameters(statement, parameters);
            log.debug("query : {}", sql);

            try (ResultSet resultSet = statement.executeQuery()) {
                final List<Object> rows = new ArrayList<>();
                while (resultSet.next()) {
                    rows.add(rowMapper.mapRow(resultSet));
                }
                return rows;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("조회에 실패했습니다.", exception);
        }
    }

    private void setParameters(final PreparedStatement statement, final Object[] parameters) throws SQLException {
        for (int index = 0; index < parameters.length; index++) {
            statement.setObject(index + 1, parameters[index]);
        }
    }
}
