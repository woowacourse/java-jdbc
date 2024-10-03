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

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, Object... args) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            for (int i = 1; i < args.length + 1; i++) {
                pstmt.setObject(i, args[i - 1]);
            }

            ResultSet rs = pstmt.executeQuery();
            log.debug("query : {}", sql);
            List<T> queryResult = new ArrayList<>();
            while (rs.next()) {
                queryResult.add(rowMapper.mapRow(rs));
            }
            return queryResult;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, Object... args) {
        List<T> queryResult = query(sql, rowMapper, args);
        if (queryResult.size() > 1) {
            throw new DataAccessException("단건 데이터 조회에서 다중 데이터가 조회되었습니다");
        }
        if (queryResult.isEmpty()) {
            return null;
        }
        return queryResult.get(0);
    }
}
