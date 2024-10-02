package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
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
        sql = sql.replace("?", "'%s'");
        sql = sql.formatted(args);
        log.info("query = {}", sql);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("sql 실행 과정에서 문제가 발생하였습니다.", e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, @Nullable Object... args) {
        sql = sql.replace("?", "'%s'");
        sql = sql.formatted(args);
        log.info("query = {}", sql);

        List<T> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                T object = rowMapper.mapRow(rs, rs.getRow());
                result.add(object);
            }

            return result;
        } catch (SQLException e) {
            throw new DataAccessException("sql 실행 과정에서 문제가 발생하였습니다.", e);
        }
    }
}
