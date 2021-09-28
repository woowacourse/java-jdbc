package nextstep.exception;

public class ObjectCreationException extends RuntimeException{

    public ObjectCreationException(Exception e) {
        super(e);
    }

    public ObjectCreationException(String message) {
        super(message);
    }
}
