package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.exception.DataAccessException;

public abstract class AbstractJdbcTemplate {

    private final DataSource dataSource;

    public AbstractJdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    protected void executeUpdate(StatementStrategy stmt) {
        try (Connection conn = dataSource.getConnection();
            PreparedStatement ps = stmt.makePreparedStatement(conn);
        ) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    protected <T> T executeQuery(StatementStrategy stmt, ResultSetExtractor<T> rse) {
        try (Connection conn = dataSource.getConnection();
            PreparedStatement ps = stmt.makePreparedStatement(conn);
            ResultSet rs = ps.executeQuery();
        ) {
            return rse.extractData(rs);
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
        } finally {

        }
    }
}
