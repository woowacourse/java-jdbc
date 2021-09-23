package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RowMapperResultSetExtractor<T> {

    private final RowMapper<T> rowMapper;
    private final int rowsExpected;

    public RowMapperResultSetExtractor(RowMapper<T> rowMapper) {
        this(rowMapper, 0);
    }

    public RowMapperResultSetExtractor(RowMapper<T> rowMapper, int rowsExpected) {
        if (Objects.isNull(rowMapper))
            throw new IllegalStateException("RowMapper is required");
        this.rowMapper = rowMapper;
        this.rowsExpected = rowsExpected;
    }

    public List<T> extractData(ResultSet rs) throws SQLException {
        List<T> results;
        if (rowsExpected > 0) {
            results = new ArrayList<>(rowsExpected);
        } else {
            results = new ArrayList<>();
        }

        int rowNum = 0;
        while (rs.next()) {
            results.add(rowMapper.mapRow(rs, rowNum++));
        }
        return results;
    }
}
