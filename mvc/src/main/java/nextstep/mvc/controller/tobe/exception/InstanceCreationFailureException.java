package nextstep.mvc.controller.tobe.exception;

public class InstanceCreationFailureException extends RuntimeException {

    public InstanceCreationFailureException(ReflectiveOperationException exception) {
        super(exception);
    }
}
