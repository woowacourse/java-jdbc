package nextstep.jdbc;

public abstract class AbstractExecution<T> implements Execution<T> {

    private final String sql;
    protected final Object[] arguments;

    public AbstractExecution(String sql, Object[] arguments) {
        this.sql = sql;
        this.arguments = arguments;
    }

    @Override
    public String getSql() {
        return sql;
    }
}
