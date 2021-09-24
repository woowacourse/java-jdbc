package nextstep.jdbc;

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

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... args) {
        return connect(sql, PreparedStatement::executeUpdate, args);
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper, Object... args) {
        return connect(sql, pstmt -> {
            try (ResultSet resultSet = pstmt.executeQuery()) {
                List<T> result = new ArrayList<>();
                for (int i = 0; resultSet.next(); i++) {
                    result.add(rowMapper.mapRow(resultSet, i));
                }

                return result;
            }
        }, args);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> results = queryForList(sql, rowMapper, args);

        if(results.size() == 1) {
            return results.get(0);
        }

        throw new IllegalStateException("결과가 한개가 아닙니다.");
    }

    private <T> T connect(String sql, Logic<T> logic, Object... args) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            for (int i = 1; i <= args.length; i++) {
                preparedStatement.setObject(i, args[i - 1]);
            }

            return logic.doLogic(preparedStatement);
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    interface Logic<T> {
        T doLogic(PreparedStatement pstmt) throws SQLException;
    }
}
