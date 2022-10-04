package nextstep.mvc.argumentResolver;

import java.lang.reflect.Parameter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class HttpServletRequestArgumentResolver implements ArgumentResolver {

	@Override
	public boolean support(Parameter parameter) {
		return parameter.getType().equals(HttpServletRequest.class);
	}

	@Override
	public Object resolve(HttpServletRequest request, HttpServletResponse response, Parameter parameter) {
		return request;
	}
}
