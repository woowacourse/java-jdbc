package com.techcourse.support.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@WebFilter("/*")
public class ResourceFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(ResourceFilter.class);

    private static final List<String> resourcePrefixs = new ArrayList<>();

    static {
        resourcePrefixs.addAll(Arrays.asList(
                "/css",
                "/js",
                "/assets",
                "/fonts",
                "/images",
                "/favicon.ico"
        ));
    }

    private RequestDispatcher requestDispatcher;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        this.requestDispatcher = filterConfig.getServletContext().getNamedDispatcher("default");
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        final var req = (HttpServletRequest) request;
        final var path = req.getRequestURI().substring(req.getContextPath().length());
        if (isResourceUrl(path)) {
            log.debug("path : {}", path);
            requestDispatcher.forward(request, response);
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean isResourceUrl(final String url) {
        for (String prefix : resourcePrefixs) {
            if (url.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void destroy() {
    }
}
