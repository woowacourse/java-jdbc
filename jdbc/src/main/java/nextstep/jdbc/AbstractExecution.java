package nextstep.jdbc;

public abstract class AbstractExecution<T> implements Execution<T> {

    protected final String sql;
    private final Object[] arguments;

    public AbstractExecution(String sql, Object[] arguments) {
        this.sql = sql;
        this.arguments = arguments;
    }
}
