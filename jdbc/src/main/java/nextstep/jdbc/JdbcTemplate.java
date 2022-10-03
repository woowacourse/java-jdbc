package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
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
        execute(sql, preparedStatementByParams(params));
    }

    private Consumer<PreparedStatement> preparedStatementByParams(final Object[] params) {
        return pstmt -> {
            int index = 1;
            for (Object param : params) {
                try {
                    pstmt.setObject(index++, param);
                } catch (SQLException e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException();
                }
            }
        };
    }

    public void execute(final String sql, final Consumer<PreparedStatement> consumer) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            consumer.accept(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> queryForList(final String sql, final Function<ResultSet, T> rowMapper, final Object... params) {
        return queryForList(sql, preparedStatementByParams(params), rowMapper);
    }

    public <T> List<T> queryForList(final String sql, final Consumer<PreparedStatement> consumer, final Function<ResultSet, T> rowMapper) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            consumer.accept(pstmt);

            try (ResultSet rs = pstmt.executeQuery()) {
                List<T> result = new ArrayList<>();
                while(rs.next()) {
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
        return queryForObject(sql, preparedStatementByParams(params), rowMapper);
    }

    public <T> T queryForObject(final String sql, final Consumer<PreparedStatement> consumer, final Function<ResultSet, T> rowMapper) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            consumer.accept(pstmt);

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
}
