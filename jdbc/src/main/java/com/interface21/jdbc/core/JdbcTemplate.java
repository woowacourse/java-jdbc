package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.RowMapper;
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

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement psmt = conn.prepareStatement(sql)
        ) {
            setPreparedStatementParameters(args, psmt);
            ResultSet rs = psmt.executeQuery();

            return mapResultSetToList(rowMapper, rs);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void setPreparedStatementParameters(Object[] args, PreparedStatement psmt) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            psmt.setObject(i + 1, args[i]);
        }
    }

    private <T> List<T> mapResultSetToList(RowMapper<T> rowMapper, ResultSet rs) throws SQLException {
        List<T> result = new ArrayList<>();
        while (rs.next()) {
            T element = rowMapper.mapRow(rs, rs.getRow());
            result.add(element);
        }
        return result;
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> result = query(sql, rowMapper, args);
        if (result.size() != 1) {
            throw new DataAccessException("조회하려는 데이터가 여러 개입니다.");
        }
        return result.get(0);
    }

    public int update(String sql, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement psmt = conn.prepareStatement(sql)
        ) {
            setPreparedStatementParameters(args, psmt);

            return psmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
