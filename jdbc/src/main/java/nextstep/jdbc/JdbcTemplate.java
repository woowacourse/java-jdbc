package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

public class JdbcTemplate {

    private static final int FIRST_INDEX = 0;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... args) {
        PreparedStatementFactory psf = new PreparedStatementFactory(dataSource, sql);
        try {
            psf.generatePreparedStatement(args).executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("[ERROR] update", e);
        } finally {
            psf.closeResources();
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        PreparedStatementFactory psf = new PreparedStatementFactory(dataSource, sql);
        try (ResultSet resultSet = psf.generatePreparedStatement(args).executeQuery()) {
            return rawMapping(rowMapper, resultSet);
        } catch (SQLException e) {
            throw new DataAccessException("[ERROR] query", e);
        } finally {
            psf.closeResources();
        }
    }

    private <T> List<T> rawMapping(final RowMapper<T> rowMapper, final ResultSet resultSet) throws SQLException {
        List<T> result = new ArrayList<>();
        while (resultSet.next()) {
            result.add(rowMapper.mapping(resultSet));
        }
        return result;
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        List<T> result = query(sql, rowMapper, args);
        return Optional.ofNullable(result.get(FIRST_INDEX));
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
