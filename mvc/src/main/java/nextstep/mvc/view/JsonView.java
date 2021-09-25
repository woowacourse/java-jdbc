package nextstep.mvc.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import nextstep.web.support.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonView implements View {

    private static final Logger LOG = LoggerFactory.getLogger(JsonView.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String jsonData = toJsonData(model);
        LOG.debug("render Json Data\n{}", jsonData);

        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        response.getWriter().write(jsonData);
    }

    private String toJsonData(Map<String, ?> data) throws JsonProcessingException {
        if (data.size() > 1) {
            return MAPPER.writeValueAsString(data);
        }

        return MAPPER.writeValueAsString(toSingleData(data));
    }

    private Object toSingleData(Map<String, ?> model) {
        return model.values()
            .stream()
            .findFirst()
            .orElseThrow(IllegalStateException::new);
    }

    @Override
    public String getViewName() {
        throw new IllegalStateException();
    }
}
