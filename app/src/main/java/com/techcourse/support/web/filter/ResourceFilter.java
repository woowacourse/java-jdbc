package com.techcourse.support.web.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;

@WebFilter("/*")
public class ResourceFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(ResourceFilter.class);

    private static final List<String> resourcePrefixes = new ArrayList<>();

    static {
        resourcePrefixes.addAll(Arrays.asList(
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
        final var req = (HttpServletRequest)request;
        final var path = req.getRequestURI().substring(req.getContextPath().length());
        if (isResourceUrl(path)) {
            log.debug("path : {}", path);
            requestDispatcher.forward(request, response);
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean isResourceUrl(final String url) {
        for (String prefix : resourcePrefixes) {
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
