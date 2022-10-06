package nextstep.jdbc.exception;

public class ParameterSettingException extends DataAccessException {

    public ParameterSettingException() {
        super("파라미터 세팅에 실패하였습니다.");
    }
}
