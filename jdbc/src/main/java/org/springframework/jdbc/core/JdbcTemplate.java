package org.springframework.jdbc.core;

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

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(new PreparedStatementCallback<T>() {
            @Override
            public T doPreparedStatement(final PreparedStatement pstmt) throws SQLException {
                ResultSet rs;

                setPrepareStatement(pstmt, args);
                rs = pstmt.executeQuery();

                log.debug("query : {}", sql);
                if (rs.next()) {
                    return rowMapper.mapRow(rs);
                }
                return null;
            }

            @Override
            public String getSql() {
                return sql;
            }
        });
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        return execute(new PreparedStatementCallback<ArrayList<T>>() {
            @Override
            public ArrayList<T> doPreparedStatement(final PreparedStatement pstmt) throws SQLException {
                ResultSet rs;

                rs = pstmt.executeQuery();

                log.debug("query : {}", sql);
                ArrayList<T> objects = new ArrayList<>();
                while (rs.next()) {
                    objects.add(rowMapper.mapRow(rs));
                }
                return objects;
            }

            @Override
            public String getSql() {
                return sql;
            }
        });
    }

    public int update(String sql, Object... args) {
        return execute(new PreparedStatementCallback<Integer>() {
            @Override
            public Integer doPreparedStatement(final PreparedStatement pstmt) throws SQLException {
                log.debug("query : {}", sql);

                setPrepareStatement(pstmt, args);

                return pstmt.executeUpdate();
            }

            @Override
            public String getSql() {
                return sql;
            }
        });
    }

    private PreparedStatement setPrepareStatement(final PreparedStatement pstmt, final Object... args)
            throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
        return pstmt;
    }

    private  <T> T execute(PreparedStatementCallback callback) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(callback.getSql())
        ) {
            return (T) callback.doPreparedStatement(pstmt);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
