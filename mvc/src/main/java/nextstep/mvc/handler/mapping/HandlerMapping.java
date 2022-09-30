package nextstep.mvc.handler.mapping;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

public interface HandlerMapping {

    void initialize();

    Optional<Object> getHandler(HttpServletRequest request);
}
