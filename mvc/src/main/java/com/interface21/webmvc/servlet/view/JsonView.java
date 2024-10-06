package com.interface21.webmvc.servlet.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interface21.web.http.MediaType;
import com.interface21.webmvc.servlet.View;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class JsonView implements View {

    private static final int SINGLE_OBJECT = 1;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void render(final Map<String, ?> model,
                       final HttpServletRequest request,
                       final HttpServletResponse response) throws Exception {
        if (model == null || model.isEmpty()) {
            return;
        }
        render(model, response);
        setContentType(response);
    }

    private void render(final Map<String, ?> model, final HttpServletResponse response) throws IOException {
        final Object renderObject = toJsonObject(model);
        final PrintWriter writer = response.getWriter();
        objectMapper.writeValue(writer, renderObject);
    }

    private Object toJsonObject(final Map<String, ?> model) {
        if (model.size() == SINGLE_OBJECT) {
            return model.values().iterator().next();
        }
        return model;
    }

    private void setContentType(final HttpServletResponse response) {
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
    }
}
