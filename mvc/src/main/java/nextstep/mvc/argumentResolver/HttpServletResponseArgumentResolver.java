package nextstep.mvc.argumentResolver;

import java.lang.reflect.Parameter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class HttpServletResponseArgumentResolver implements ArgumentResolver {

	@Override
	public boolean support(Parameter parameter) {
		return parameter.getType().equals(HttpServletResponse.class);
	}

	@Override
	public Object resolve(HttpServletRequest request, HttpServletResponse response, Parameter parameter) {
		return response;
	}
}
