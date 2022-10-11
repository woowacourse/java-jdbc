package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.utils.DataAccessUtils;

public class JdbcTemplate extends JdbcSupporter{

    public JdbcTemplate(final DataSource dataSource) {
        super(dataSource);
    }

    public void update(final String sql, final Object...args) {
        execute(sql, PreparedStatement::executeUpdate, args);
    }

    public <T> T queryForObject(final String sql, RowMapper<T> rowMapper, final Object...args) {
        final List<T> results = query(sql, rowMapper, args);
        return DataAccessUtils.nullableSingleResult(results);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, preparedStatement -> getResult(rowMapper, preparedStatement), args);
    }

    private <T> List<T> getResult(final RowMapper<T> rowMapper,
                                  final PreparedStatement preparedStatement) throws SQLException {
        final ResultSet resultSet = preparedStatement.executeQuery();
        int row = 0;
        final List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet, row++));
        }
        return results;
    }

    public DataSource getDataSource() {
        return super.getDataSource();
    }
}
