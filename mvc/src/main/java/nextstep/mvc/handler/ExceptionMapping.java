package nextstep.mvc.handler;

public interface ExceptionMapping {

    void initialize();

    Object getHandler(RuntimeException exception);
}
