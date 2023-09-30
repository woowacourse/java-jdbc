package webmvc.org.springframework.web.servlet.mvc.tobe;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webmvc.org.springframework.web.servlet.ModelAndView;
import webmvc.org.springframework.web.servlet.View;

import java.io.IOException;
import java.util.Map;

public class DispatcherServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(DispatcherServlet.class);

    private HandlerMappings handlerMappings;
    private HandlerAdapters handlerAdapters;

    public DispatcherServlet() {
    }

    @Override
    public void init() {
        this.handlerMappings = new HandlerMappings();
        this.handlerAdapters = new HandlerAdapters();

        handlerMappings.init();
        handlerAdapters.init();
    }


    @Override
    protected void service(final HttpServletRequest request, final HttpServletResponse response) {
        log.debug("Method : {}, Request URI : {}", request.getMethod(), request.getRequestURI());
        try {
            final HandlerExecution mappedHandler = handlerMappings.getHandler(request);
            final HandlerAdapter handlerAdapter = handlerAdapters.getHandlerAdapter(mappedHandler.getMethod());

            final ModelAndView modelAndView = handlerAdapter.handle(request, response, mappedHandler);

            final View view = modelAndView.getView();
            final Map<String, Object> model = modelAndView.getModel();

            view.render(model, request, response);
        } catch (Exception e) {
            sendError(response, 404);
            log.error("exception occurred : {}", e.getMessage(), e);
        } catch (Error e) {
            sendError(response, 500);
            log.error("error occurred : {}", e.getMessage(), e);
        }
    }

    private void sendError(HttpServletResponse response, final int statusCode) {
        try {
            response.sendError(statusCode);
        } catch (IOException e) {
            throw new ErrorRenderingException(e);
        }
    }
}
