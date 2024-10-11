package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.EmptyResultDataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
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
    private static final int ONE_RESULT_SIZE = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> results = query(sql, rowMapper, args);
        if (results.isEmpty()) {
            throw new EmptyResultDataAccessException();
        }
        if (results.size() == ONE_RESULT_SIZE) {
            return results.getFirst();
        }
        throw new IncorrectResultSizeDataAccessException(results.size());
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            PreparedStatementSetter parameterSetter = new ArgumentPreparedStatementSetter(args);
            parameterSetter.setValues(pstmt);
            ResultSet resultSet = pstmt.executeQuery();

            return mapResults(rowMapper, resultSet);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> mapResults(RowMapper<T> rowMapper, ResultSet resultSet) {
        List<T> results = new ArrayList<>();
        try {
            while (resultSet.next()) {
                T row = rowMapper.map(resultSet);
                results.add(row);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
        return results;
    }

    public int update(String sql, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            PreparedStatementSetter statementSetter = new ArgumentPreparedStatementSetter(args);
            statementSetter.setValues(pstmt);

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }
}
