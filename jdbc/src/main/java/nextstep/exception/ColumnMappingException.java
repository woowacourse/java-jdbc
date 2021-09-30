package nextstep.exception;

public class ColumnMappingException extends DataAccessException {

    public ColumnMappingException(String message) {
        super(message);
    }

    public ColumnMappingException(String message, Throwable cause) {
        super(message, cause);
    }
}
