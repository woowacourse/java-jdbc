package nextstep.jdbc;

import exception.DataAccessException;
import exception.IncorrectDataSizeException;
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

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // TODO 클래스로 분리할 부분 찾기
    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    interface Result<T> {

        T makeResult(PreparedStatement preparedStatement) throws SQLException;
    }

    public int update(String sql, Object... args) {
        log(sql);

        return execute(sql, PreparedStatement::executeUpdate, args);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        log(sql);

        List<T> results = queryForList(sql, rowMapper, args);
        if (results.size() != 1) {
            throw new IncorrectDataSizeException(1, results.size());
        }
        return results.get(0);
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper, Object... args) {
        log(sql);

        return execute(
            sql,
            preparedStatement -> {
                try (ResultSet rs = preparedStatement.executeQuery();) {
                    List<T> results = new ArrayList<>();
                    int rowNum = 0;
                    while (rs.next()) {
                        results.add(rowMapper.mapRow(rs, rowNum++));
                    }
                    return results;
                }
            },
            args
        );
    }

    public <T> T execute(String sql, Result<T> result, Object... args) {
        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            return result.makeResult(preparedStatement);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void log(String sql) {
        log.info("query: {}", sql);
    }

}
