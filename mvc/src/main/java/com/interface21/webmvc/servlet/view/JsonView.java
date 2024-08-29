package com.interface21.webmvc.servlet.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.interface21.web.http.MediaType;
import com.interface21.webmvc.servlet.View;

import java.io.IOException;
import java.util.Map;

public class JsonView implements View {

    @Override
    public void render(final Map<String, ?> model, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        if (model == null || model.isEmpty()) {
            return;
        }

        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);

        final Object renderObject = toJsonObject(model);
        render(renderObject, response.getOutputStream());
    }

    private void render(final Object renderObject, final ServletOutputStream outputStream) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(outputStream, renderObject);
    }

    private Object toJsonObject(final Map<String, ?> model) {
        if (model.size() == 1) {
            return model.values()
                    .stream()
                    .findFirst()
                    .orElseThrow(IllegalArgumentException::new);
        }
        return model;
    }
}
