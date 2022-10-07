package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class JdbcTemplate extends JdbcSupporter{

    public JdbcTemplate(final DataSource dataSource) {
        super(dataSource);
    }

    public void update(final String sql, final Object...args) {
        execute(sql, PreparedStatement::executeUpdate, args);
    }

    public <T> T queryForObject(final String sql, RowMapper<T> rowMapper, final Object...args) {
        return execute(sql, preparedStatement -> {
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return rowMapper.mapRow(resultSet, resultSet.getRow());
            }
            return null;
        }, args);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, preparedStatement -> {
            final ResultSet resultSet = preparedStatement.executeQuery();
            int row = 0;
            final List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet, row++));
            }
            return results;
        }, args);
    }
}
