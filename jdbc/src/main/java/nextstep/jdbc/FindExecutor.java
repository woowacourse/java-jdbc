package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class FindExecutor<T> implements QueryExecutor<List<T>> {

    private final RowMapper<T> rowMapper;

    public FindExecutor(final RowMapper<T> rowMapper) {
        this.rowMapper = rowMapper;
    }

    @Override
    public List<T> executePreparedStatement(final PreparedStatementStarter pss) throws SQLException {
        ResultSet resultSet = pss.executeQuery();
        return rowMapper.mapRow(resultSet);
    }
}
