package com.interface21.webmvc.servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

public interface View {
    void render(final Map<String, ?> model, final HttpServletRequest request, final HttpServletResponse response) throws Exception;
}
