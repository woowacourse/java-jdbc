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

    public int update(String sql, Object... args) {
        return update(sql, new ArgumentPreparedStatementSetter(args));
    }

    public int update(String sql, PreparedStatementSetter pstmtSetter) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmtSetter.setValues(pstmt);

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> T selectOne(String sql, ResultMapper<T> resultMapper, Object... args) {
        return selectOne(sql, resultMapper, new ArgumentPreparedStatementSetter(args));
    }

    public <T> T selectOne(String sql, ResultMapper<T> resultMapper, PreparedStatementSetter pstmtSetter) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = executeQuery(pstmtSetter, pstmt)) {

            if (!rs.next()) {
                throw new DataAccessException("Expected 1 result, but found 0");
            }

            T result = resultMapper.mapResult(rs);

            if (rs.next()) {
                throw new DataAccessException("Expected 1 result, but found more than 1");
            }

            return result;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> selectList(String sql, ResultMapper<T> resultMapper, Object... args) {
        return selectList(sql, resultMapper, new ArgumentPreparedStatementSetter(args));
    }

    public <T> List<T> selectList(String sql, ResultMapper<T> resultMapper, PreparedStatementSetter pstmtSetter) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = executeQuery(pstmtSetter, pstmt)
        ) {
            List<T> results = new ArrayList<>();

            while (rs.next()) {
                results.add(resultMapper.mapResult(rs));
            }

            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private ResultSet executeQuery(PreparedStatementSetter pstmtSetter, PreparedStatement pstmt) throws SQLException {
        pstmtSetter.setValues(pstmt);
        return pstmt.executeQuery();
    }
}

