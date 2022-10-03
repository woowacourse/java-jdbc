package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final String sql, final Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = preparedStatementCreator(params).apply(conn, sql)
        ) {
            log.debug("query : {}", sql);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> queryForList(final String sql, final Function<ResultSet, T> rowMapper, final Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = preparedStatementCreator(params).apply(conn, sql)
        ) {
            log.debug("query : {}", sql);

            try (ResultSet rs = pstmt.executeQuery()) {
                List<T> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(rowMapper.apply(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(final String sql, final Function<ResultSet, T> rowMapper, final Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = preparedStatementCreator(params).apply(conn, sql)
        ) {
            log.debug("query : {}", sql);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rowMapper.apply(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private BiFunction<Connection, String, PreparedStatement> preparedStatementCreator(final Object... params) {
        return (conn, sql) -> {
            try {
                final PreparedStatement pstmt = conn.prepareStatement(sql);
                int index = 1;
                for (Object param : params) {
                    pstmt.setObject(index++, param);
                }
                return pstmt;
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException();
            }
        };
    }
}
