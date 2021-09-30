package nextstep.jdbc.exception;

public class QueryException extends JdbcException{

    private static final String MESSAGE = "조회 실패";

    public QueryException() {
        this(MESSAGE);
    }

    private QueryException(String message) {
        super(message);
    }
}
