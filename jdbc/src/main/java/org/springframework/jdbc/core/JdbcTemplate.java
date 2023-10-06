package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

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

    public int update(final String sql, final Object... parameters) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = PreparedStateUtil.makeStatement(conn, sql, parameters)
        ) {
            log.debug("Execute Update - query : {}", sql);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = PreparedStateUtil.makeStatement(conn, sql, parameters);
             ResultSet rs = pstmt.executeQuery()
        ) {
            log.debug("Execute Query -query : {}", sql);
            return SingleResult.makeSingleResultFrom(makeResults(rowMapper, rs));
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> makeResults(final RowMapper<T> rowMapper, final ResultSet rs) throws SQLException {
        List<T> list = new ArrayList<>();
        while (rs.next()) {
            list.add(rowMapper.mapping(rs));
        }
        return list;
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()
        ) {
            log.debug("Execute Query - query : {}", sql);
            return makeResults(rowMapper, rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }
}
