package com.interface21.jdbc.core;

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
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            setParameters(pstmt, params);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> readAll(String sql, RowMapper<T> rowMapper, Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            setParameters(pstmt, params);

            try (ResultSet resultSet = pstmt.executeQuery()) {
                List<T> result = new ArrayList<>();
                while (resultSet.next()) {
                    result.add(rowMapper.rowMap(resultSet));
                }

                return result;
            }

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
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

    private void setParameters(PreparedStatement pstmt, Object... params) throws SQLException {
        for (int index = 0; index < params.length; index++) {
            int sqlParamIndex = index + 1;
            pstmt.setObject(sqlParamIndex, params[index]);
        }
    }
}
