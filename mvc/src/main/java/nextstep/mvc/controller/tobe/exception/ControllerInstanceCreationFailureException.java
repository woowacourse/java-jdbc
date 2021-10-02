package nextstep.mvc.controller.tobe.exception;

public class ControllerInstanceCreationFailureException extends RuntimeException {

    public ControllerInstanceCreationFailureException(ReflectiveOperationException exception) {
        super(exception);
    }
}
