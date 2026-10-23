package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class SelectJdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(SelectJdbcTemplate.class);

    public final Object query() {
        final var sql = createQuery();

        try (Connection connection = getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            final List<Object> results = new ArrayList<>();
            try (ResultSet resultSet = executeQuery(statement)) {
                while (resultSet.next()) {
                    results.add(mapRow(resultSet));
                }
            }
            return results;
        } catch (SQLException e) {
            throw new DataAccessException("SQL 실행에 실패했습니다: " + sql, e);
        }
    }

    private ResultSet executeQuery(final PreparedStatement statement) throws SQLException {
        setValues(statement);
        return statement.executeQuery();
    }

    protected abstract String createQuery();

    protected abstract DataSource getDataSource();

    protected abstract Object mapRow(ResultSet resultSet) throws SQLException;

    protected abstract void setValues(PreparedStatement statement) throws SQLException;
}
