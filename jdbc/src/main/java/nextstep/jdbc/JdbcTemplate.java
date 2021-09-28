package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String query, Object... args) {
        executeUpdate((conn) -> {
            PreparedStatement ps = conn.prepareStatement(query);
            setParameters(ps, args);
            return ps;
        });
    }

    private void setParameters(PreparedStatement ps, Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            ps.setObject(i + 1, args[i]);
        }
    }

    public <T> T query(String sql, ResultSetExtractor<T> rse, Object... args) {
        return executeQuery(
            (conn) -> {
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                setParameters(preparedStatement, args);
                return preparedStatement;
            },
            rse
        );
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        return executeQuery(
            (conn) -> {
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                setParameters(preparedStatement, args);
                return preparedStatement;
            },
            (rs) -> {
                if (rs.next()) {
                    return rowMapper.mapRow(rs, 1);
                }
                return null;
            }
        );
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        return executeQuery(
            (conn) -> conn.prepareStatement(sql),
            rowMapper
        );
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return executeQuery(
            (conn) -> {
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                setParameters(preparedStatement, args);
                return preparedStatement;
            },
            rowMapper
        );
    }

    protected void executeUpdate(StatementStrategy stmt) {
        execute(stmt, PreparedStatement::executeUpdate);
    }

    private <T> T execute(StatementStrategy stmt, Executor<T> executor) {
        try (Connection conn = dataSource.getConnection();
            PreparedStatement ps = stmt.makePreparedStatement(conn);
        ) {
            return executor.doSomething(ps);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    protected <T> T executeQuery(StatementStrategy stmt, ResultSetExtractor<T> rse) {
        return execute(stmt, (ps) -> {
            try (ResultSet rs = ps.executeQuery()) {
                return rse.extractData(rs);
            }
        });
    }

    protected <T> List<T> executeQuery(StatementStrategy stmt, RowMapper<T> rowMapper) {
        try (Connection conn = dataSource.getConnection();
            PreparedStatement ps = stmt.makePreparedStatement(conn);
            ResultSet rs = ps.executeQuery();
        ) {
            List<T> result = new ArrayList<>();

            int rowNum = 1;
            while (rs.next()) {
                result.add(rowMapper.mapRow(rs, rowNum));
                rowNum++;
            }
            return result;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private interface Executor<T> {
        T doSomething(PreparedStatement ps) throws SQLException;
    }
}
