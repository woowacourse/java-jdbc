package nextstep.exception;

public class BadSqlGrammarException extends DataAccessException {

    public BadSqlGrammarException(String message) {
        super(message);
    }

    public BadSqlGrammarException(String message, Throwable cause) {
        super(message, cause);
    }
}
