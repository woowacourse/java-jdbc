package nextstep.mvc.controller.tobe;

public class ControllerInstanceCreationFailureException extends RuntimeException {

    public ControllerInstanceCreationFailureException(ReflectiveOperationException exception) {
        super(exception);
    }
}
