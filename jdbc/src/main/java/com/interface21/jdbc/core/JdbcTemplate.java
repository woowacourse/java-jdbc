package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.ResultNotSingleException;
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

    public void write(String sql, Object... params) {
        execute(sql, PreparedStatement::executeUpdate, params);
    }

    public <T> List<T> readAll(String sql, RowMapper<T> rowMapper, Object... params) {
        return execute(sql, pstmt -> mapResultSetToList(rowMapper, pstmt), params);
    }

    private static <T> List<T> mapResultSetToList(RowMapper<T> rowMapper, PreparedStatement pstmt) throws SQLException {
        try (ResultSet resultSet = pstmt.executeQuery()) {
            List<T> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(rowMapper.rowMap(resultSet));
            }

            return result;
        }
    }

    public <T> T read(String sql, RowMapper<T> rowMapper, Object... params) {
        List<T> result = readAll(sql, rowMapper, params);
        if (result.size() > 1) {
            throw new ResultNotSingleException(result.size());
        }

        if (result.isEmpty()) {
            return null;
        }

        return result.getFirst();
    }

    private <T> T execute(String sql, PreparedStatementExecutor<T> executor, Object... params) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            PreparedStatementSetter.setParameters(pstmt, params);
            return executor.execute(pstmt);

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }
}
