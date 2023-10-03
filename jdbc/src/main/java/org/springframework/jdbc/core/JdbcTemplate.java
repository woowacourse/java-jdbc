package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = prepareMappedStatement(conn, sql, args);) {

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.warn("쿼리 실행 도중에 오류가 발생하였습니다. {} ====> SQL = {} {} ====> Args = {}",
                    System.lineSeparator(), sql, System.lineSeparator(), args, e);
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement prepareMappedStatement(final Connection conn, final String sql, final Object[] args) throws SQLException {
        final PreparedStatement pstmt = conn.prepareStatement(sql);
        for (int argumentIndex = 1; argumentIndex <= args.length; argumentIndex++) {
            pstmt.setObject(argumentIndex, args[argumentIndex - 1]);
        }

        return pstmt;
    }

    public <T> T queryObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = prepareMappedStatement(conn, sql, args);
             final ResultSet rs = pstmt.executeQuery();) {

            while (rs.next()) {
                return rowMapper.toObject(rs);
            }

            return null;
        } catch (SQLException e) {
            log.warn("쿼리 실행 도중에 오류가 발생하였습니다. {} ====> SQL = {} {} ====> Args = {}",
                    System.lineSeparator(), sql, System.lineSeparator(), args, e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql);
             final ResultSet rs = pstmt.executeQuery();) {

            final List<T> queriedData = new ArrayList<>();
            while (rs.next()) {
                queriedData.add(rowMapper.toObject(rs));
            }

            return queriedData;
        } catch (SQLException e) {
            log.warn("조회하던 도중에 오류가 발생하였습니다. {} ====> SQL = {}",
                    System.lineSeparator(), sql, e);
            throw new RuntimeException(e);
        }
    }
}
