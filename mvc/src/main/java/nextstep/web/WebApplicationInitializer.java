package nextstep.web;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

public interface WebApplicationInitializer {
    void onStartup(ServletContext servletContext) throws ServletException;
}
