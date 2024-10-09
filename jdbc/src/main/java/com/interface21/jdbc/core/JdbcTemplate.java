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
import javax.annotation.Nullable;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, @Nullable Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query = {}", sql);
            PreparedStatementSetter pss = createArgsPreparedStatementSetter(args);
            pss.setValues(pstmt);

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("sql 실행 과정에서 문제가 발생하였습니다.", e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, @Nullable Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query = {}", sql);
            PreparedStatementSetter pss = createArgsPreparedStatementSetter(args);
            pss.setValues(pstmt);

            return getQueryResult(rowMapper, pstmt);
        } catch (SQLException e) {
            throw new DataAccessException("sql 실행 과정에서 문제가 발생하였습니다.", e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, @Nullable Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query = {}", sql);
            PreparedStatementSetter pss = createArgsPreparedStatementSetter(args);
            pss.setValues(pstmt);

            List<T> result = getQueryResult(rowMapper, pstmt);
            return requiredSingleResult(result);
        } catch (SQLException e) {
            throw new DataAccessException("sql 실행 과정에서 문제가 발생하였습니다.", e);
        }
    }

    private <T> List<T> getQueryResult(RowMapper<T> rowMapper, PreparedStatement pstmt) throws SQLException {
        try (ResultSet rs = pstmt.executeQuery()) {
            List<T> result = new ArrayList<>();
            while (rs.next()) {
                T object = rowMapper.mapRow(rs, rs.getRow());
                result.add(object);
            }

            return result;
        }
    }

    private <T> T requiredSingleResult(List<T> result) {
        if (result.isEmpty()) {
            throw new EmptyResultDataAccessException("데이터 개수가 0개입니다.");
        }
        if (result.size() > 1) {
            throw new IncorrectResultSizeDataAccessException("데이터 개수가 올바르지 않습니다. (size: %d)".formatted(result.size()));
        }
        return result.get(0);
    }

    public PreparedStatementSetter createArgsPreparedStatementSetter(@Nullable Object[] args) {
        return new ArgumentPreparedStatementSetter(args);
    }
}
