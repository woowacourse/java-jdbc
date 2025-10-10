package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeUpdate(String sql, Object... params) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            validationParamLength(params, ps);
            bindingParams(params, ps);

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            validationParamLength(params, ps);
            bindingParams(params, ps);

            try (ResultSet rs = ps.executeQuery()) {
                return extractObject(rs, rowMapper);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }

            return results;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void bindingParams(Object[] params, PreparedStatement ps) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    private void validationParamLength(Object[] params, PreparedStatement pstmt) throws SQLException {
        int parameterCount = pstmt.getParameterMetaData().getParameterCount();
        if (params.length != parameterCount) {
            throw new DataAccessException(
                    String.format("파라미터 개수가 일치하지 않습니다.: SQL에 %d개 필요, %d개 제공됨",
                            parameterCount,
                            params.length
                    )
            );
        }
    }

    private <T> T extractObject(final ResultSet rs, final RowMapper<T> rowMapper) throws SQLException {
        if (rs.next()) {
            T result = rowMapper.mapRow(rs);
            if (rs.next()) {
                throw new DataAccessException("결과가 2개 이상입니다.");
            }
            return result;
        }
        throw new DataAccessException("결과가 없습니다.");
    }
}
