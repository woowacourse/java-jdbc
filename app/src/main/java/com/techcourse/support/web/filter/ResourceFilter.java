package com.techcourse.support.web.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public void init(FilterConfig filterConfig) throws ServletException {
        this.requestDispatcher = filterConfig.getServletContext().getNamedDispatcher("default");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        final HttpServletRequest req = (HttpServletRequest) request;
        final String path = req.getRequestURI().substring(req.getContextPath().length());
        if (isResourceUrl(path)) {
            log.debug("path : {}", path);
            requestDispatcher.forward(request, response);
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean isResourceUrl(String url) {
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
