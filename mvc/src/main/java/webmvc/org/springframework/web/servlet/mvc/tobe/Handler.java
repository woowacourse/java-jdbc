package webmvc.org.springframework.web.servlet.mvc.tobe;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface Handler {

    boolean isSupport();

    Object handle(final HttpServletRequest request, final HttpServletResponse response) throws Exception;
}
