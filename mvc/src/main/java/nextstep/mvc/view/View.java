package nextstep.mvc.view;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface View {

    void render(final Map<String, ?> model, final HttpServletRequest request, final HttpServletResponse response) throws
        Exception;
}
