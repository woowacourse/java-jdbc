package nextstep.jdbc.templates;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.sql.DataSource;
import nextstep.jdbc.utils.ObjectConverter;
import nextstep.jdbc.utils.RowMapper;
import nextstep.jdbc.utils.RowMapperListExtractor;
import nextstep.jdbc.utils.preparestatement.PreparedStatementSetter;

public class JdbcTemplate extends BaseJdbcTemplate {

    public JdbcTemplate(DataSource dataSource) {
        super(dataSource);
    }

    public <T> List<T> queryForList(String sql, Class<T> type, Object... args) {
        return query(sql, (rs, rowNum) -> ObjectConverter.convertSingleObject(rs, type), args);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return query(sql, new RowMapperListExtractor<>(rowMapper), args);
    }

    public <T> List<T> query(String sql, RowMapperListExtractor<T> resultSetExtractor, Object... args) {
        if (Objects.isNull(sql) || Objects.isNull(resultSetExtractor) || sql.isEmpty()) {
            throw new IllegalStateException("sql or resultSetExtractor can not be null");
        }

        if (args.length == 0) {
            return execute(stmt -> {
                try (ResultSet resultSet = stmt.executeQuery(sql)) {
                    return callBackForm(resultSetExtractor, resultSet);
                }
            });
        }

        return execute(
            con -> getPreparedStatement(sql, con, args),
            ps -> {
                try (ResultSet resultSet = ps.executeQuery()) {
                    return callBackForm(resultSetExtractor, resultSet);
                }
            }
        );
    }

    public <T> Optional<T> queryForObject(String sql, Class<T> type, Object... args) {
        return queryForList(sql, type, args).stream().findAny();
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        return query(sql, rowMapper, args).stream().findAny();
    }

    public int update(String sql, Object... args) {
        if (args.length == 0) {
            return execute(stmt -> stmt.executeUpdate(sql));
        }
        return execute(
            con -> getPreparedStatement(sql, con, args),
            PreparedStatement::executeUpdate
        );
    }

    private <T> List<T> callBackForm(RowMapperListExtractor<T> resultSetExtractor, ResultSet resultSet) {
        return resultSetExtractor.extractData(resultSet);
    }

    private PreparedStatement getPreparedStatement(String sql, Connection con, Object[] args)
        throws SQLException {
        PreparedStatement preparedStatement = con.prepareStatement(sql);
        PreparedStatementSetter.psmtSet(preparedStatement, args);
        return preparedStatement;
    }
}
