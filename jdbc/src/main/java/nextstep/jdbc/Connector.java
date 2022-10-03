package nextstep.jdbc;

public interface Connector {

    <T> T execute(String sql, QueryExecutor<T> queryExecutor, Object... parameters);
}
