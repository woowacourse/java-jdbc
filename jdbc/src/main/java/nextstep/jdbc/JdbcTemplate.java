package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public int insert(final String sql, final Object... params) {
        try (
                final var connection = getConnection();
                final var pstmt = connection.prepareStatement(sql);
        ) {
            log.debug("query : {}", sql);
            setParams(pstmt, List.of(params));

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T select(final String sql, final Function<ResultSet, T> mapper, final Object... params) {
        try (
                final var connection = getConnection();
                final var pstmt = connection.prepareStatement(sql);
        ) {
            log.debug("query : {}", sql);
            setParams(pstmt, List.of(params));
            final var resultSet = pstmt.executeQuery();

            return mapper.apply(resultSet);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setParams(final PreparedStatement pstmt, final List<Object> params) throws SQLException {
        if (Objects.isNull(params)) {
            return;
        }

        for (int i = 0; i < params.size(); i++) {
            pstmt.setObject(i + 1, params.get(i));
        }
    }
}
