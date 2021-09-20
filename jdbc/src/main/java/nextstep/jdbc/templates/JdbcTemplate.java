package nextstep.jdbc.templates;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.sql.DataSource;
import nextstep.jdbc.operations.QueryOperations;
import nextstep.jdbc.utils.ObjectConverter;
import nextstep.jdbc.utils.ResultSetExtractor;
import nextstep.jdbc.utils.RowMapper;
import nextstep.jdbc.utils.RowMapperListExtractor;
import nextstep.jdbc.utils.preparestatement.PreparedStatementSetter;

public class JdbcTemplate extends BaseJdbcTemplate implements QueryOperations {

    public JdbcTemplate(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public <T> List<T> queryForList(String sql, Class<T> type, Object... args) throws SQLException {
        return query(sql, (rs, rowNum) -> ObjectConverter.convertSingleObject(rs, type), args);
    }

    @Override
    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args)
        throws SQLException {
        return query(sql, new RowMapperListExtractor<>(rowMapper), args);
    }

    @Override
    public <T> T query(String sql, ResultSetExtractor<T> resultSetExtractor, Object... args)
        throws SQLException {
        if (Objects.isNull(sql) || Objects.isNull(resultSetExtractor) || sql.isEmpty()) {
            throw new IllegalStateException("sql or resultSetExtractor can not be null");
        }

        if (args.length == 0) {
            return execute(stmt -> callBackForm(resultSetExtractor, stmt.executeQuery(sql)));
        }

        return execute(
            con -> getPreparedStatement(sql, con, args),
            ps -> callBackForm(resultSetExtractor, ps.executeQuery())
        );
    }

    @Override
    public <T> Optional<T> queryForObject(String sql, Class<T> type, Object... args)
        throws SQLException {
        return queryForList(sql, type, args).stream().findAny();
    }

    @Override
    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... args)
        throws SQLException {
        return query(sql, rowMapper, args).stream().findAny();
    }

    @Override
    public int update(String sql, Object... args) throws SQLException {
        if (args.length == 0) {
            return execute(stmt -> stmt.executeUpdate(sql));
        }
        return execute(
            con -> getPreparedStatement(sql, con, args),
            PreparedStatement::executeUpdate
        );
    }

    private <T> T callBackForm(ResultSetExtractor<T> resultSetExtractor, ResultSet resultSet)
        throws SQLException {
        try (resultSet) {
            return resultSetExtractor.extractData(resultSet);
        }
    }

    private PreparedStatement getPreparedStatement(String sql, Connection con, Object[] args)
        throws SQLException {
        PreparedStatement preparedStatement = con.prepareStatement(sql);
        PreparedStatementSetter.psmtSet(preparedStatement, args);
        return preparedStatement;
    }
}
