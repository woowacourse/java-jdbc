package nextstep.mvc.handler.adapter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nextstep.mvc.handler.HandlerExecution;
import nextstep.mvc.view.JspView;
import nextstep.mvc.view.ModelAndView;

public class AnnotationHandlerAdapter implements HandlerAdapter {

	@Override
	public boolean supports(Object handler) {
		return handler instanceof HandlerExecution;
	}

	@Override
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		HandlerExecution handlerExecution = (HandlerExecution)handler;
		Object result = handlerExecution.handle(request, response);
		return handleResult(result);
	}

	private ModelAndView handleResult(Object result) {
		if (result instanceof ModelAndView) {
			return (ModelAndView)result;
		}
		String viewName = (String)result;
		return new ModelAndView(new JspView(viewName));
	}
}
