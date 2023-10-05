package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final QueryTemplate queryTemplate;

    public JdbcTemplate(final DataSource dataSource) {
        this.queryTemplate = new QueryTemplate(dataSource);
    }

    public void update(Connection conn, String sql, Object... parameters) {
        queryTemplate.query(conn, sql, PreparedStatement::executeUpdate, parameters);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... parameters) {
        return queryTemplate.query(sql, (pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                log.debug("query : {}", sql);
                if (!rs.first()) {
                    throw new DataAccessException("조건에 맞는 데이터가 없습니다.");
                }
//                if (rs.last() && rs.getRow() > 1) {
//                    throw new DataAccessException("단건 조회에 조회된 데이터가 2개 이상입니다.");
//                }
                return rowMapper.map(rs);
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
