package com.interface21.jdbc.core;

import com.interface21.PreparedStatementSetter;
import com.interface21.RowMapper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JdbcTemplate<T> {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    protected abstract String createQuery();

    public abstract DataSource getDataSource();

    public List<T> query(RowMapper<T> rowMapper) {
        return query(rowMapper, null);
    }

    public List<T> query(RowMapper<T> rowMapper, PreparedStatementSetter setter) {
        final var sql = createQuery();

        try (Connection conn = getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            if (setter != null) {
                setter.setValues(pstmt);
            }
            try (ResultSet rs = executeQuery(pstmt)) {
                List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs, rs.getRow()));
                }
                return results;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void update(PreparedStatementSetter setter) {
        String sql = createQuery();

        try (Connection conn = getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            setter.setValues(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private ResultSet executeQuery(final PreparedStatement pstmt) throws SQLException {
        return pstmt.executeQuery();
    }
}
