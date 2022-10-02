package nextstep.mvc.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import nextstep.mvc.exception.ViewException;
import nextstep.web.support.MediaType;
import org.apache.commons.lang3.ClassUtils;

public class JsonView implements View {

    private static final int MULTI_DATA_NUMBER = 2;
    private static final int SINGLE_DATA_NUMBER = 1;
    private static final String NO_DATA = "";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void render(final Map<String, ?> model, final HttpServletRequest request,
                       final HttpServletResponse response) {
        response.setCharacterEncoding("utf-8");
        response.addHeader("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);

        writeData(response, toJson(model));
    }

    private String toJson(final Map<String, ?> model) {
        if (model.size() >= MULTI_DATA_NUMBER) {
            return toJsonWithObject(model);
        }
        if (model.size() >= SINGLE_DATA_NUMBER) {
            return toJsonWithSingle(model);
        }
        return NO_DATA;
    }

    private String toJsonWithObject(final Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (final JsonProcessingException e) {
            throw new ViewException("Failed to serialize a object", e);
        }
    }

    private String toJsonWithSingle(final Map<String, ?> model) {
        final Object firstValue = getFirstValue(model);
        if (ClassUtils.isPrimitiveOrWrapper(firstValue.getClass())) {
            return String.valueOf(firstValue);
        }
        return toJsonWithObject(firstValue);
    }

    private Object getFirstValue(final Map<String, ?> model) {
        return model.keySet()
                .stream()
                .map(model::get)
                .findFirst()
                .orElseThrow(() -> new ViewException("There is nothing in a model."));
    }

    private void writeData(final HttpServletResponse response, final String value) {
        try {
            response.getWriter().write(value);
        } catch (final IOException | NullPointerException e) {
            throw new ViewException("Failed to write data to response.", e);
        }
    }
}
