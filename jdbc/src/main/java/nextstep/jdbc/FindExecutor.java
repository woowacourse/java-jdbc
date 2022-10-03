package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class FindExecutor<T> implements QueryExecutor<List<T>> {

    private final RowMapper<T> rowMapper;

    public FindExecutor(RowMapper<T> rowMapper) {
        this.rowMapper = rowMapper;
    }

    @Override
    public List<T> executePreparedStatement(final PreparedStatement ps) throws SQLException {
        ResultSet resultSet = ps.executeQuery();
        return rowMapper.mapRow(resultSet);
    }
}
