package nextstep.jdbc;

import nextstep.exception.JdbcInternalException;
import nextstep.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RowMapperResultSetExtractor<T> implements ResultSetExtractor<List<T>> {

    private final Logger log = LoggerFactory.getLogger(RowMapperResultSetExtractor.class);

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
        } catch (SQLException e) {
            log.info("JdbcInternalException: {} {}", e.getMessage(), e.getCause());
            throw new JdbcInternalException("JdbcInternalException: " + e.getMessage(), e.getCause());
        }
    }

    private List<T> initializeList() {
        if (this.rowsExpected > 0) {
            return new ArrayList<>(this.rowsExpected);
        }
        return new ArrayList<>();
    }
}
