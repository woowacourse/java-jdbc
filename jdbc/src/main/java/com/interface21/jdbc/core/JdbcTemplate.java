package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
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

    public <T> List<T> query(
            final String sql,
            final RowMapper<T> rowMapper,
            final Object... params
    ) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            setParameters(pstmt, params);

            try (ResultSet resultSet = pstmt.executeQuery()) {
                List<T> results = new ArrayList<>();

                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet));
                }

                return results;
            }

        } catch (SQLException e) {
            throw new DataAccessException("SQL 실행 실패 : " + sql, e);
        }
    }

    public <T> T queryForObject(
            final String sql,
            final RowMapper<T> rowMapper,
            final Object... params
    ) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            log.debug("query : {}, sql");
            setParameters(pstmt, params);

            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (!resultSet.next()) {
                    throw new DataAccessException("조회 결과가 없습니다.");
                }

                T result = rowMapper.mapRow(resultSet);

                if (resultSet.next()) {
                    throw new DataAccessException("조회 결과는 1행이여야 합니다. 2행 이상 조회되었습니다.");
                }

                return result;
            }
        } catch (SQLException e) {
            throw new DataAccessException("SQL 실행 실패 : " + sql, e);
        }
    }

    public int update(final String sql, final Object... params) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            setParameters(pstmt, params);

            return pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("SQL 실행 실패 : " + sql, e);
        }
    }

    private void setParameters(
            final PreparedStatement pstmt,
            final Object... params
    ) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }
}
