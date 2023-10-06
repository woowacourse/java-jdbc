package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

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

    public void update(final String sql, PreparedStatementSetter preparedStatementSetter) throws DataAccessException {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                preparedStatementSetter.setValues(pstmt);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> T query(final String sql, final RowMapper<T> rowMapper, final PreparedStatementSetter preparedStatementSetter) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            try (final ResultSet rs = executeQuery(preparedStatementSetter, pstmt)) {
                log.debug("query : {}", sql);
                if (rs.next()) {
                    return rowMapper.mapRow(rs, rs.getRow());
                }
                throw new DataAccessException("Empty Result");
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private ResultSet executeQuery(final PreparedStatementSetter preparedStatementSetter, PreparedStatement preparedStatement) throws SQLException {
        preparedStatementSetter.setValues(preparedStatement);
        return preparedStatement.executeQuery();
    }

    public <T> T query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            try (final ResultSet rs = executeQuery(args, pstmt)) {
                if (rs.next()) {
                    return rowMapper.mapRow(rs, rs.getRow());
                }
                throw new DataAccessException("Empty Result");
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private ResultSet executeQuery(final Object[] args, final PreparedStatement pstmt) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
        return pstmt.executeQuery();
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) throws DataAccessException {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            try (final ResultSet rs = pstmt.executeQuery()) {
                List<T> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(rowMapper.mapRow(rs, rs.getRow()));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
