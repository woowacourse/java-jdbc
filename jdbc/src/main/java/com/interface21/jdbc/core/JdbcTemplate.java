package com.interface21.jdbc.core;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, PreparedStatementSetter pss) {
        List<T> result = query(sql, rowMapper, pss);
        if (result.isEmpty()) {
            throw new DataAccessException("조회된 데이터가 없습니다.");
        }
        if (result.size() > 1) {
            throw new DataAccessException("조회된 데이터가 두 건 이상입니다.");
        }
        return result.getFirst();
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, PreparedStatementSetter pss) {
        return execute(sql, pstmt -> {
            pss.setObject(pstmt);
            try (ResultSet rs = pstmt.executeQuery()) {
                return mapResults(rs, rowMapper);
            }
        });
    }

    private <T> List<T> mapResults(ResultSet rs, RowMapper<T> rowMapper) throws SQLException {
        List<T> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rowMapper.mapRow(rs));
        }
        return result;
    }

    public void update(String sql, PreparedStatementSetter pss) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        execute(connection, sql, pstmt -> {
            pss.setObject(pstmt);
            return pstmt.executeUpdate();
        });
    }

    private <T> T execute(String sql, PreparedStatementExecutor<T> executor) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            return executor.execute(pstmt);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> void execute(Connection connection, String sql, PreparedStatementExecutor<T> executor) {
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            executor.execute(pstmt);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
