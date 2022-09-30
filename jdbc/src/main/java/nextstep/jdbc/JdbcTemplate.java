package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> executeQuery(String sql, ParameterSource parameterSource, RowMapper<T> rowMapper) {
        try (final var conn = getConnection();
             final var pstmt = conn.prepareStatement(sql)) {
            setParams(pstmt, parameterSource);
            return query(pstmt, rowMapper);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public void executeUpdate(String sql, ParameterSource parameterSource) {
        try (final var conn = getConnection();
             final var pstmt = conn.prepareStatement(sql)) {
            setParams(pstmt, parameterSource);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void setParams(PreparedStatement pstmt, ParameterSource parameterSource) throws SQLException {
        for (var index = 0; index < parameterSource.getParamCount(); index++) {
            pstmt.setObject(index + 1, parameterSource.getParam(index));
        }
    }

    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (NullPointerException e) {
            throw new IllegalStateException("DataSource가 설정되지 않았습니다.");
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> query(PreparedStatement pstmt, RowMapper<T> rowMapper) throws SQLException {
        try (final var rs = pstmt.executeQuery()) {
            List<T> users = new ArrayList<>();
            if (rs.next()) {
                users.add(rowMapper.mapRow(rs));
            }
            return users;
        }
    }
}
