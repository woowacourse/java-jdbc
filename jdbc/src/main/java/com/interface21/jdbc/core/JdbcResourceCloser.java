package com.interface21.jdbc.core;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class JdbcResourceCloser {

    private static final Logger log = LoggerFactory.getLogger(JdbcResourceCloser.class);

    static void close(final AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            close(resource);
        }
    }

    static void close(final AutoCloseable resource) {
        if (Objects.isNull(resource)) {
            return;
        }
        try {
            resource.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private JdbcResourceCloser() {}
}
