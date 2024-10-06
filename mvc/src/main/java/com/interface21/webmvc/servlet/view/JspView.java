package com.interface21.webmvc.servlet.view;

import com.interface21.webmvc.servlet.View;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JspView implements View {

    private static final Logger log = LoggerFactory.getLogger(JspView.class);

    public static final String REDIRECT_PREFIX = "redirect:";

    private final String viewName;

    public JspView(final String viewName) {
        this.viewName = Objects.requireNonNull(viewName, "viewName is null. 이동할 URL을 입력하세요.");
    }

    @Override
    public void render(final Map<String, ?> model,
                       final HttpServletRequest request,
                       final HttpServletResponse response) throws Exception {
        log.debug("ViewName : {}", viewName);
        if (isRedirect()) {
            handleRedirect(response);
            return;
        }
        setRequestAttributes(model, request);
        forwardRequest(request, response);
    }

    private boolean isRedirect() {
        return viewName.startsWith(REDIRECT_PREFIX);
    }

    private void handleRedirect(final HttpServletResponse response) throws IOException {
        final String redirectUrl = viewName.substring(REDIRECT_PREFIX.length());
        response.sendRedirect(redirectUrl);
    }

    private void setRequestAttributes(final Map<String, ?> model, final HttpServletRequest request) {
        model.forEach((key, value) -> {
            log.debug("attribute name : {}, value : {}", key, value);
            request.setAttribute(key, value);
        });
    }

    private void forwardRequest(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        final RequestDispatcher requestDispatcher = request.getRequestDispatcher(viewName);
        requestDispatcher.forward(request, response);
    }
}
