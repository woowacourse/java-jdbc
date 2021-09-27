package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.exception.DataAccessException;

public class AbstractJdbcTemplate {

    private final DataSource dataSource;

    protected AbstractJdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    protected void executeUpdate(StatementStrategy stmt) {
        execute(stmt, PreparedStatement::executeUpdate);
    }

    protected <T> T executeQuery(StatementStrategy stmt, ResultSetExtractor<T> rse) {
        return execute(stmt, new executor<T>() {
            @Override
            public T doSomething(PreparedStatement ps) throws SQLException {
                try (ResultSet rs = ps.executeQuery()) {
                    return rse.extractData(rs);
                }
            }
        });
    }

    private <T> T execute(StatementStrategy stmt, executor<T> executor) {
        try (Connection conn = dataSource.getConnection();
            PreparedStatement ps = stmt.makePreparedStatement(conn);
        ) {
          return executor.doSomething(ps);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
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

    private interface executor<T> {
        T doSomething(PreparedStatement ps) throws SQLException;
    }
}
