package nextstep.mvc.view;

import java.io.PrintWriter;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nextstep.web.support.MediaType;

public class JsonView implements View {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void render(final Map<String, ?> model, final HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
		PrintWriter writer = response.getWriter();
		writer.write(convertToJson(model));
	}

	private String convertToJson(Map<String, ?> model) throws JsonProcessingException {
		if (model.size() == 1) {
            return objectMapper.writeValueAsString(getFirstValue(model));
        }
        return objectMapper.writeValueAsString(model);
    }

	private Object getFirstValue(Map<String, ?> model) {
		return model.keySet().stream()
			.map(model::get)
			.findFirst()
			.orElseThrow();
	}
}
