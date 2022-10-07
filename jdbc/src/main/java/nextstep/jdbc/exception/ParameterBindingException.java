package nextstep.jdbc.exception;

public class ParameterBindingException extends RuntimeException {

    private static final String MESSAGE = "파라미터 바인딩에 실패했습니다.";

    public ParameterBindingException() {
        super(MESSAGE);
    }
}
