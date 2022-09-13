package nextstep.mvc;

import jakarta.servlet.http.HttpServletRequest;

public interface HandlerMapping {

    void initialize();

    Object getHandler(final HttpServletRequest request);
}
