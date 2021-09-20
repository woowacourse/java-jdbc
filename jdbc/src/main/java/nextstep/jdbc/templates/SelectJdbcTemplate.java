package nextstep.jdbc.templates;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.sql.DataSource;
import nextstep.jdbc.operations.SelectOperations;
import nextstep.jdbc.utils.ObjectConverter;
import nextstep.jdbc.utils.ResultSetExtractor;
import nextstep.jdbc.utils.RowMapper;
import nextstep.jdbc.utils.RowMapperListExtractor;

public class SelectJdbcTemplate extends BaseJdbcTemplate implements SelectOperations {

    public SelectJdbcTemplate(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public <T> List<T> queryForList(String sql, Class<T> type) throws SQLException {
        return query(sql, (rs, rowNum) -> ObjectConverter.convertSingleObject(rs, type));
    }

    @Override
    public <T> List<T> query(String sql, RowMapper<T> rowMapper) throws SQLException {
        return query(sql, new RowMapperListExtractor<>(rowMapper));
    }

    @Override
    public <T> T query(String sql, ResultSetExtractor<T> resultSetExtractor) throws SQLException {
        if (Objects.isNull(sql) || Objects.isNull(resultSetExtractor) || sql.isEmpty()) {
            throw new IllegalStateException("sql or resultSetExtractor can not be null");
        }

        return execute(stmt -> {
            try (ResultSet resultSet = stmt.executeQuery(sql)) {
                return resultSetExtractor.extractData(resultSet);
            }
        });
    }

    @Override
    public <T> Optional<T> queryForObject(String sql, Class<T> type) throws SQLException {
        return queryForList(sql, type).stream().findAny();
    }

    @Override
    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper) throws SQLException {
        return query(sql, rowMapper).stream().findAny();
    }
}
