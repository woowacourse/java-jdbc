package nextstep.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RowMapperResultSetExtractor<T> {
    private static final Logger log = LoggerFactory.getLogger(RowMapperResultSetExtractor.class);

    private final RowMapper<T> rowMapper;

    public RowMapperResultSetExtractor(RowMapper<T> rowMapper) {
        this.rowMapper = rowMapper;
    }

    public List<T> extractData(ResultSet rs) throws SQLException {
        List<T> results = new ArrayList<>();
        while (rs.next()) {
            results.add(this.rowMapper.mapRow(rs));
        }
        return results;
    }
}
