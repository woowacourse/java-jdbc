package nextstep.jdbc;

public class DataAccessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DataAccessException() {
        super();
    }

    public DataAccessException(Throwable cause) {
        super(cause);
    }
}
