package com.interface21.webmvc.servlet.mvc;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.webmvc.servlet.ModelAndView;
import com.interface21.webmvc.servlet.mvc.tobe.HandlerExecution;
import com.interface21.webmvc.servlet.mvc.tobe.HandlerExecutionHandlerAdapter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DatabaseErrorLoggingTest {

    @Test
    void logsDatabaseFailureOnceAndPreservesCause() throws Exception {
        final var dataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        final var statement = mock(PreparedStatement.class);
        final var failure = new SQLException("Database unavailable");
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("delete from users")).thenReturn(statement);
        when(statement.executeUpdate()).thenThrow(failure);

        final var controller = new FailingController(new JdbcTemplate(dataSource));
        final var method = FailingController.class.getMethod("handle", HttpServletRequest.class,
                HttpServletResponse.class);
        final var handler = new HandlerExecution(controller, method);
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        final var mapping = mock(HandlerMapping.class);
        when(mapping.getHandler(request)).thenReturn(handler);
        final var servlet = new DispatcherServlet();
        servlet.addHandlerMapping(mapping);
        servlet.addHandlerAdapter(new HandlerExecutionHandlerAdapter());
        servlet.init();

        final var logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        final var appender = new ListAppender<ILoggingEvent>();
        appender.start();
        logger.addAppender(appender);
        try {
            assertThatThrownBy(() -> servlet.service(request, response))
                    .isInstanceOf(ServletException.class)
                    .hasRootCause(failure);

            assertThat(appender.list)
                    .filteredOn(event -> event.getLevel() == Level.ERROR)
                    .extracting(ILoggingEvent::getLoggerName)
                    .containsExactly(DispatcherServlet.class.getName());
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    public static class FailingController {

        private final JdbcTemplate jdbcTemplate;

        public FailingController(final JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
        }

        public ModelAndView handle(final HttpServletRequest request, final HttpServletResponse response) {
            jdbcTemplate.update("delete from users");
            return null;
        }
    }
}
