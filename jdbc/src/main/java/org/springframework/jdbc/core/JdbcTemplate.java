package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final QueryTemplate queryTemplate;

    public JdbcTemplate(final DataSource dataSource) {
        this.queryTemplate = new QueryTemplate(dataSource);
    }

    public void update(String sql, Object... parameters) {
        queryTemplate.query(sql, PreparedStatement::executeUpdate, parameters);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... parameters) {
        return queryTemplate.query(sql, (pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                log.debug("query : {}", sql);
                if (rs.next()) {
                    return rowMapper.map(rs);
                }
                throw new NoSuchElementException("조건에 맞는 데이터가 없습니다.");
            }
        }), parameters);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... parameters) {
        return queryTemplate.query(sql, (pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                log.debug("query : {}", sql);
                List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(rowMapper.map(rs));
                }
                return results;
            }
        }), parameters);
    }
}
