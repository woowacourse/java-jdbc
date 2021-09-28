package nextstep.exception;

public class DataAccessException extends RuntimeException {

    public DataAccessException(Exception e) {
        super(e);
    }

    public DataAccessException(String message) {
        super(message);
    }
}
