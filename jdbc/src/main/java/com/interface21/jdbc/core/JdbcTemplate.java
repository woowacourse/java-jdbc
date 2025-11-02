package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.callback.PreparedStatementCallback;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.jdbc.mapper.RowMapper;
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

    public void update(String sql, final Object... args) {
        execute(
                sql,
                pstmt -> {
                    setParameter(pstmt, args);
                    return pstmt.executeUpdate();
                }
        );
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper,
                                final Object... args) {
        return execute(
                sql,
                pstmt -> {
                    setParameter(pstmt, args);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            return rowMapper.mapRowToResult(rs);
                        }
                        return null;
                    }
                }
        );
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper) {
        return execute(
                sql,
                pstmt -> {
                    try (ResultSet rs = pstmt.executeQuery()) {
                        List<T> resultList = new ArrayList<>();
                        while (rs.next()) {
                            T result = rowMapper.mapRowToResult(rs);
                            resultList.add(result);
                        }
                        return resultList;
                    }
                }
        );
    }

    private void setParameter(final PreparedStatement pstmt, final Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }

    private <T> T execute(
            final String sql,
            final PreparedStatementCallback<T> callback
    ) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            log.debug("query : {}", sql);

            return callback.run(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }
}
