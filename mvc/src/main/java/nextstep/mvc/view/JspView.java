package nextstep.mvc.view;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JspView implements View {

    private static final Logger log = LoggerFactory.getLogger(JspView.class);

    private static final String REDIRECT_PREFIX = "redirect:";

    private final String viewName;

    public JspView(final String viewName) {
        this.viewName = viewName;
    }

    @Override
    public void render(final Map<String, ?> model, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        if (isRedirect()) {
            response.sendRedirect(extractRedirectUrl());
            return;
        }
        setModelAttribute(model, request);
        forward(request, response);
    }

    private boolean isRedirect() {
        return viewName.startsWith(REDIRECT_PREFIX);
    }

    private String extractRedirectUrl() {
        return viewName.substring(JspView.REDIRECT_PREFIX.length());
    }

    private void setModelAttribute(Map<String, ?> model, HttpServletRequest request) {
        model.keySet().forEach(key -> {
            log.debug("attribute name : {}, value : {}", key, model.get(key));
            request.setAttribute(key, model.get(key));
        });
    }

    private void forward(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final var requestDispatcher = request.getRequestDispatcher(viewName);
        requestDispatcher.forward(request, response);
    }

    public String getViewName() {
        return viewName;
    }
}
