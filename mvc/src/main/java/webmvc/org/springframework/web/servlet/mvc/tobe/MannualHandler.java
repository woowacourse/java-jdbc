package webmvc.org.springframework.web.servlet.mvc.tobe;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import webmvc.org.springframework.web.servlet.mvc.asis.Controller;

public class MannualHandler implements Handler {

    private final Controller controller;

    public MannualHandler(final Controller controller) {
        this.controller = controller;
    }

    @Override
    public boolean isSupport() {
        return controller != null;
    }

    @Override
    public Object handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return controller.execute(request, response);
    }

    public Controller getController() {
        return controller;
    }
}
