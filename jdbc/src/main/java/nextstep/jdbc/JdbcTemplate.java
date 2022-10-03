package nextstep.jdbc;

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

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(String sql, Object... parameters) {
        Executable<Void> executable = preparedStatement -> {
            preparedStatement.executeUpdate();
            return null;
        };
        executeQuery(executable, sql, parameters);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... parameters) {
        List<T> result = query(sql, rowMapper, parameters);
        if (result.size() != 1) {
            throw new DataAccessException("결과는 " + result.size() + "가 아닌 1개여야합니다.");
        }

        return result.get(0);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... parameters) {
        Executable<List<T>> executable = preparedStatement -> {
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                List<T> result = new ArrayList<>();
                while (resultSet.next()) {
                    result.add(rowMapper.mapRow(resultSet));
                }
                return result;
            }
        };
        return executeQuery(executable, sql, parameters);
    }

    private  <T> T executeQuery(Executable<T> executable, String sql, Object... parameters) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            setParameters(preparedStatement, parameters);
            log.debug("query : {}", sql);

            return executable.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setParameters(PreparedStatement preparedStatement, Object[] parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            preparedStatement.setObject(i + 1, parameters[i]);
        }
    }
}
