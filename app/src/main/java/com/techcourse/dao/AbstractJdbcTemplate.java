package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.PreparedStatementSetter;
import com.interface21.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractJdbcTemplate {

    private final DataSource dataSource;

    protected AbstractJdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final PreparedStatementSetter pss) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(createQuery())) {
            pss.setValues(pstmt);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(final RowMapper<T> rowMapper) {
        return query(rowMapper, pstmt -> {
        });
    }

    public <T> List<T> query(final RowMapper<T> rowMapper, final PreparedStatementSetter pss) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(createQuery())) {
            pss.setValues(pstmt);
            try (ResultSet rs = pstmt.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
                return results;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> T queryForObject(final RowMapper<T> rowMapper, final PreparedStatementSetter pss) {
        List<T> results = query(rowMapper, pss);
        if (results.size() != 1) {
            throw new DataAccessException("조회 결과가 1개가 아닙니다. size = " + results.size());
        }
        return results.get(0);
    }

    protected abstract String createQuery();
}
