package nextstep.jdbc.utils;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RowMapperListExtractor<T> {

    private static final Logger log = LoggerFactory.getLogger(TransactionManager.class);

    private final RowMapper<T> rowMapper;

    public RowMapperListExtractor(RowMapper<T> rowMapper) {
        this.rowMapper = rowMapper;
    }

    public List<T> extractData(ResultSet rs) {
        List<T> result = new ArrayList<>();
        try {
            int rowNum = 0;
            while (rs.next()) {
                result.add(this.rowMapper.mapRow(rs, rowNum++));
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            JdbcResourceCloser.closeResultSet(rs);
        }
        return result;
    }
}
