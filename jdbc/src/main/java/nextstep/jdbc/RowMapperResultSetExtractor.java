package nextstep.jdbc;

import nextstep.util.Assert;

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
        Assert.notNull(rowMapper, "RowMapper is required");
        this.rowMapper = rowMapper;
        this.rowsExpected = rowsExpected;
    }

    @Override
    public List<T> extractData(ResultSet rs) {
        List<T> results = initializeList();
        int rowNum = 0;
        try {
            while (rs.next()) {
                results.add(this.rowMapper.mapRow(rs, rowNum++));
            }
            return results;
        } catch (SQLException exception) {
            throw new IllegalStateException("데이터 베이스 연결 오류입니다");
        }
    }

    private List<T> initializeList() {
        if (this.rowsExpected > 0) {
            return new ArrayList<>(this.rowsExpected);
        }
        return new ArrayList<>();
    }
}
