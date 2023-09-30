package org.springframework.jdbc.core;

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

    public void execute(String sql, Object... params) {
        try (PreparedStatement pstmt = preparedStatementWithParams(sql, params)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        try (PreparedStatement pstmt = preparedStatementWithParams(sql, params)) {
            ResultSet resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                return rowMapper.mapRow(resultSet);
            }
            throw new RuntimeException();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper, Object... params) {
        try (PreparedStatement pstmt = preparedStatementWithParams(sql, params)) {
            ResultSet resultSet = pstmt.executeQuery();
            List<T> result = new ArrayList<>();
            if (resultSet.next()) {
                result.add(rowMapper.mapRow(resultSet));
            }
            return result;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement preparedStatementWithParams(String sql, Object... params) throws SQLException {
        PreparedStatement pstmt = dataSource.getConnection().prepareStatement(sql);
        log.debug("query : {}", sql);
        for (int i = 1; i <= params.length; i++) {
            pstmt.setObject(i, params[i - 1]);
        }
        return pstmt;
    }
}
