package org.springframework.jdbc.core;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Long executeUpdate(final String sql, final Object... parameters) {
        try (final Connection connection = dataSource.getConnection();
            final PreparedStatement pstmt = connection.prepareStatement(sql,
                RETURN_GENERATED_KEYS)) {
            log.debug("query : {}", sql);
            setPreparedStatement(pstmt, parameters);
            pstmt.executeUpdate();
            return extractId(pstmt);
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private Long extractId(final PreparedStatement pstmt) throws SQLException {
        try (final ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getLong(1);
            }
            throw new SQLException("id를 찾을 수 없습니다.");
        }
    }

    private void setPreparedStatement(
        final PreparedStatement pstmt,
        final Object[] parameters
    ) throws SQLException {
        for (int index = 1; index <= parameters.length; index++) {
            pstmt.setObject(index, parameters[index - 1]);
        }
    }

    public <T> T executeQuery(
        final String sql,
        final Mapper<T> mapper,
        final Object... objects
    ) {
        try (final Connection connection = dataSource.getConnection();
            final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setPreparedStatement(pstmt, objects);
            final ResultSet rs = pstmt.executeQuery();
            log.debug("query : {}", sql);
            if (rs.next()) {
                return mapper.map(rs);
            }
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
