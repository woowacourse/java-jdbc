package nextstep.mvc.view;

import static nextstep.web.support.MediaType.APPLICATION_JSON_UTF8_VALUE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

public class JsonView implements View {

    private final ObjectMapper objectMapper;

    public JsonView() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void render(final Map<String, ?> model, final HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType(APPLICATION_JSON_UTF8_VALUE);
        final String body = getBody(model);
        response.getWriter().write(body);
    }

    private String getBody(final Map<String, ?> model) throws JsonProcessingException {
        return objectMapper.writeValueAsString(getAttribute(model));
    }

    private Object getAttribute(final Map<String, ?> model) {
        if (model.size() == 1) {
            final String key = (String) model.keySet().toArray()[0];
            return model.get(key);
        }
        return model;
    }
}
