package nextstep.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RowMapperResultSetExtractor<T> implements ResultSetExtractor<List<T>> {

    private final RowMapper<T> rowMapper;
    private final int rowsExpected;

    public RowMapperResultSetExtractor(RowMapper<T> rowMapper) {
        this(rowMapper, 0);
    }

    public RowMapperResultSetExtractor(RowMapper<T> rowMapper, int rowsExpected) {
        this.rowMapper = rowMapper;
        this.rowsExpected = rowsExpected;
    }

    @Override
    public List<T> extractData(ResultSet rs) throws SQLException {
        List<T> results = createBucket();
        while (rs.next()) {
            results.add(this.rowMapper.mapRow(rs));
        }
        rs.close();
        return results;
    }

    private ArrayList<T> createBucket() {
        if (this.rowsExpected > 0) {
            return new ArrayList<>(this.rowsExpected);
        }
        return new ArrayList<>();
    }
}
