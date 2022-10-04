package nextstep.mvc.controller.exception;

public class InvalidReflectionException extends RuntimeException {

    public InvalidReflectionException() {
        super("리플렉션을 수행할 수 없습니다.");
    }
}

