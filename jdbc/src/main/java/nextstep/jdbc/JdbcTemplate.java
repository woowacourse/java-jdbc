package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

import nextstep.jdbc.exception.EmptyResultDataAccessException;
import nextstep.jdbc.exception.IncorrectResultSizeDataAccessException;
import nextstep.jdbc.exception.JdbcTemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... params) {
        return connect(sql, PreparedStatement::executeUpdate, params);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
        return connect(
                sql,
                preparedStatement -> resultsToObject(rowMapper, preparedStatement),
                params
        );
    }

    private <T> List<T> resultsToObject(RowMapper<T> rowMapper, PreparedStatement preparedStatement) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            List<T> results = new ArrayList<>();
            for (int i = 0; resultSet.next(); i++) {
                results.add(rowMapper.mapRow(resultSet, i));
            }

            return results;
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        List<T> results = query(sql, rowMapper, params);
        validateResultsForObject(results);

        return results.get(0);
    }

    private <T> void validateResultsForObject(List<T> results) {
        if(results.size() <= 0) {
            throw new EmptyResultDataAccessException("조회할 결과가 존재하지 않습니다.");
        }

        if(results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException("조회할 결과의 개수가 두 개 이상입니다.");
        }
    }

    private <T> T connect(String sql, Query<T> query, Object... params) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
        ) {
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }

            log.debug("query : {}", sql);
            return query.doQuery(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new JdbcTemplateException(e);
        }
    }

    interface Query<T> {
        T doQuery(PreparedStatement preparedStatement) throws SQLException;
    }
}
