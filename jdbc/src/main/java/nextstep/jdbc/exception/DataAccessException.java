package nextstep.jdbc.exception;

public class DataAccessException extends RuntimeException {

    public DataAccessException() {
        super("error encountered when accessing data");
    }
}
