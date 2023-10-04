package org.springframework.jdbc.core;

public abstract class JdbcTemplateException extends RuntimeException {

    protected JdbcTemplateException(final String message) {
        super(message);
    }

    static class NoDataAccessException extends JdbcTemplateException {

        private static final String NO_DATA_ACCESS_EXCEPTION_MESSAGE = "조회하는 데이터가 없습니다.";

        NoDataAccessException() {
            super(NO_DATA_ACCESS_EXCEPTION_MESSAGE);
        }
    }


    static class MoreDataAccessException extends JdbcTemplateException {

        private static final String MORE_DATA_ACCESS_EXCEPTION_MESSAGE = "조회하는 데이터가 2개 이상 존재합니다..";

        MoreDataAccessException() {
            super(MORE_DATA_ACCESS_EXCEPTION_MESSAGE);
        }
    }


    static class DatabaseAccessException extends JdbcTemplateException {

        public DatabaseAccessException(final String message) {
            super(message);
        }
    }
}
