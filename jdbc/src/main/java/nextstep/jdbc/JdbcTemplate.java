package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.exception.IncorrectResultSizeDataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final PreparedStatementSetter preparedStatementSetter = (pstmt, args) -> {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    };

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeUpdate(String sql, Object... args) {
        execute(sql, pstmt -> {
            preparedStatementSetter.setValues(pstmt, args);
            return pstmt.executeUpdate();
        });
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(sql, pstmt -> {
            preparedStatementSetter.setValues(pstmt, args);
            ResultSet resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                return rowMapper.mapRow(resultSet);
            }
            throw new IncorrectResultSizeDataAccessException();
        });
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        return execute(sql, pstmt -> {
            ResultSet resultSet = pstmt.executeQuery();
            List<T> result = new ArrayList<>();
            while (resultSet.next()) {
                T t = rowMapper.mapRow(resultSet);
                result.add(t);
            }
            return result;
        });
    }

    private <T> T execute(String sql, PreparedStatementStrategy<T> strategy) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            return strategy.doStatement(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
