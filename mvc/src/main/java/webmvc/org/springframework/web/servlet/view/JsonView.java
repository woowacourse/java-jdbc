package webmvc.org.springframework.web.servlet.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import web.org.springframework.http.MediaType;
import webmvc.org.springframework.web.servlet.View;

import java.io.IOException;
import java.util.Map;

public class JsonView implements View {

    @Override
    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (model == null || model.isEmpty()) {
            return;
        }

        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);

        Object renderObject = toJsonObject(model);
        render(renderObject, response.getOutputStream());
    }

    private void render(Object renderObject, ServletOutputStream outputStream) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(outputStream, renderObject);
    }

    private Object toJsonObject(Map<String, ?> model) {
        if (model.size() == 1) {
            return model.values()
                    .stream()
                    .findFirst()
                    .orElseThrow(IllegalArgumentException::new);
        }
        return model;
    }
}
