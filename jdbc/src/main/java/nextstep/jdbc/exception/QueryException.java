package nextstep.jdbc.exception;

public class QueryException extends RuntimeException {

    public QueryException(String message) {
        super("[ERROR] jdbctemplate의 동작시 오류가 발생했습니다. \" action : " + message + "\"");
    }

}
