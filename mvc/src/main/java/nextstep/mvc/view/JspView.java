package nextstep.mvc.view;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import nextstep.mvc.exception.ViewException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JspView implements View {

    private static final Logger log = LoggerFactory.getLogger(JspView.class);

    public static final String REDIRECT_PREFIX = "redirect:";

    private final String viewName;

    public JspView(final String viewName) {
        this.viewName = viewName;
    }

    public static JspView withRedirectPrefix(final String viewName) {
        return new JspView(REDIRECT_PREFIX + viewName);
    }

    @Override
    public void render(final Map<String, ?> model, final HttpServletRequest request,
                       final HttpServletResponse response) {
        if (viewName.startsWith(REDIRECT_PREFIX)) {
            sendRedirect(response);
            return;
        }

        model.keySet().forEach(key -> {
            log.debug("attribute name : {}, value : {}", key, model.get(key));
            request.setAttribute(key, model.get(key));
        });

        forward(request, response);
    }

    private void sendRedirect(final HttpServletResponse response) {
        try {
            response.sendRedirect(viewName.substring(JspView.REDIRECT_PREFIX.length()));
        } catch (final IOException e) {
            throw new ViewException("Filed to send redirect response.", e);
        }
    }

    private void forward(final HttpServletRequest request, final HttpServletResponse response) {
        final var requestDispatcher = request.getRequestDispatcher(viewName);
        try {
            requestDispatcher.forward(request, response);
        } catch (final ServletException | IOException e) {
            throw new ViewException("Failed to forward request to another resource.", e);
        }
    }
}
