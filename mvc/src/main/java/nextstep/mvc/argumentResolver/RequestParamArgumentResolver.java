package nextstep.mvc.argumentResolver;

import java.lang.reflect.Parameter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nextstep.web.annotation.RequestParam;

public class RequestParamArgumentResolver implements ArgumentResolver {

	@Override
	public boolean support(Parameter parameter) {
		return parameter.isAnnotationPresent(RequestParam.class);
	}

	@Override
	public Object resolve(HttpServletRequest request, HttpServletResponse response, Parameter parameter) {
		RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
		try {
			return request.getParameter(requestParam.name());
		} catch (Exception e) {
			return null;
		}
	}
}
