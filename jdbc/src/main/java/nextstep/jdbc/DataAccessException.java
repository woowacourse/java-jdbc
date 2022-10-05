package nextstep.jdbc;

public class DataAccessException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_ERROR_MESSAGE = "Query 실행도중 오류가 발생했습니다.";

    public DataAccessException() {
        super(DEFAULT_ERROR_MESSAGE);
    }

    public DataAccessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(Throwable cause) {
        super(cause);
    }
}
