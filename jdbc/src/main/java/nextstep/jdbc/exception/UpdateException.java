package nextstep.jdbc.exception;

public class UpdateException extends JdbcException {

    private static final String MESSAGE = "삽입 또는 수정 실패";

    public UpdateException() {
        this(MESSAGE);
    }

    private UpdateException(String message) {
        super(message);
    }
}
