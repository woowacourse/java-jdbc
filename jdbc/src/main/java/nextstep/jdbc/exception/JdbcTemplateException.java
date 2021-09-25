package nextstep.jdbc.exception;

public class JdbcTemplateException extends RuntimeException{

    public JdbcTemplateException(String message) {
        super(message);
    }

    public JdbcTemplateException(Throwable cause) {
        super(cause);
    }
}
