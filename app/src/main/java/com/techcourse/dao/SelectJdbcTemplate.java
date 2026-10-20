package com.techcourse.dao;

import com.interface21.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class SelectJdbcTemplate<T> {

    private final DataSource dataSource;

    protected SelectJdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<T> query() {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(createQuery())) {
            setValues(pstmt);
            try (ResultSet rs = pstmt.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
                return results;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public T queryForObject() {
        List<T> results = query();
        if (results.size() != 1) {
            throw new DataAccessException("조회 결과가 1개가 아닙니다. size = " + results.size());
        }
        return results.get(0);
    }

    protected abstract String createQuery();

    protected abstract void setValues(final PreparedStatement pstmt) throws SQLException;

    protected abstract T mapRow(final ResultSet rs) throws SQLException;
}
