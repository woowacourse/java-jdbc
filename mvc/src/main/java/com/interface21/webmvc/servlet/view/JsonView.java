package com.interface21.webmvc.servlet.view;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interface21.web.http.MediaType;
import com.interface21.webmvc.servlet.View;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

public class JsonView implements View {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.setVisibility(objectMapper.getVisibilityChecker().withFieldVisibility(ANY));
    }

    @Override
    public void render(
            final Map<String, ?> model,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        Object value = model.size() == 1 ? model.values().iterator().next() : model;
        String json = objectMapper.writeValueAsString(value);

        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.print(json);

        response.addHeader("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);
    }
}
