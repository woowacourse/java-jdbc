package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.connector.DbConnector;
import nextstep.jdbc.connector.DbConnectorImpl;
import nextstep.jdbc.executor.QueryExecuteResult;
import nextstep.jdbc.executor.QueryExecutor;
import nextstep.jdbc.mapper.ResultSetToObjectMapper;
import nextstep.jdbc.resolver.IntMultiParameterResolver;
import nextstep.jdbc.resolver.MultiParameterResolver;
import nextstep.jdbc.resolver.MultiParameterResolvers;
import nextstep.jdbc.resolver.PreparedStatementParameterResolver;
import nextstep.jdbc.resolver.StringMultiParameterResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DbConnector dbConnector;
    private MultiParameterResolvers multiParameterResolvers;

    public JdbcTemplate(DataSource dataSource) {
        this.dbConnector = new DbConnectorImpl(dataSource);
        init();
    }

    private void init() {
        multiParameterResolvers = new MultiParameterResolvers(
            new StringMultiParameterResolver(),
            new IntMultiParameterResolver()
        );
    }

    public QueryExecuteResult executeInsertOrUpdateOrDelete(String sql,
                                                            PreparedStatementParameterResolver resolver) {
        return execute(sql, resolver);
    }

    public QueryExecuteResult executeInsertOrUpdateOrDelete(String sql,
                                                            Object... parameters) {
        return execute(sql, multiParameterBinding(parameters));
    }

    public QueryExecuteResult executeInsertOrUpdateOrDelete(String sql) {
        return executeInsertOrUpdateOrDelete(sql, PreparedStatementParameterResolver::identity);
    }

    public <T> T queryForObject(String sql,
                                ResultSetToObjectMapper<T> mapper,
                                PreparedStatementParameterResolver resolver) {
        return execute(sql, resolver, QueryForOneExecutor(mapper));
    }

    public <T> T queryForObject(String sql,
                                ResultSetToObjectMapper<T> mapper) {
        return queryForObject(sql, mapper, PreparedStatementParameterResolver::identity);
    }

    public <T> T queryForObject(String sql,
                                ResultSetToObjectMapper<T> mapper,
                                Object... parameters) {
        return queryForObject(sql, mapper, multiParameterBinding(parameters));
    }

    private PreparedStatementParameterResolver multiParameterBinding(Object[] parameters) {
        return preparedStatement -> {
            for (int i = 0; i < parameters.length; i++) {
                Object parameter = parameters[i];
                MultiParameterResolver properResolver = multiParameterResolvers
                    .findProperResolver(parameter);
                properResolver.resolve(preparedStatement, i + 1, parameter);
            }
        };
    }

    private <T> QueryExecutor<T> QueryForOneExecutor(ResultSetToObjectMapper<T> mapper) {
        return preparedStatement -> {
            preparedStatement.executeQuery();
            ResultSet rs = preparedStatement.getResultSet();
            if (rs.next()) {
                return mapper.map(rs);
            }
            throw new IllegalStateException("queryForInt의 결과값이 없습니다.");
        };
    }

    public <T> List<T> queryForMany(String sql,
                                    ResultSetToObjectMapper<T> mapper) {
        return execute(sql, PreparedStatementParameterResolver::identity,
            QueryForAllExecutor(mapper));
    }

    public <T> T queryForMany(String sql,
                              ResultSetToObjectMapper<T> mapper,
                              Object... parameters) {
        return queryForObject(sql, mapper, multiParameterBinding(parameters));
    }

    private <T> QueryExecutor<List<T>> QueryForAllExecutor(ResultSetToObjectMapper<T> mapper) {
        return preparedStatement -> {
            preparedStatement.executeQuery();
            ResultSet rs = preparedStatement.getResultSet();
            List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(mapper.map(rs));
            }
            log.info("결과 개수 : {}", results.size());
            return results;
        };
    }

    private QueryExecuteResult execute(String sql, PreparedStatementParameterResolver resolver) {
        return execute(sql,
            resolver,
            preparedStatement -> new QueryExecuteResult(preparedStatement.executeUpdate())
        );
    }

    private <T> T execute(String sql,
                          PreparedStatementParameterResolver resolver,
                          QueryExecutor<T> queryExecutor) {
        try (Connection connection = dbConnector.getConnection();
            PreparedStatement pstmt = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            resolver.resolve(pstmt);
            return queryExecutor.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException("DML 처리 중 오류가 발생했습니다.", e);
        }
    }

}
