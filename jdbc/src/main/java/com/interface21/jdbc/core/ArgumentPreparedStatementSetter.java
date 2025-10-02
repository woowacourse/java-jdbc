package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Object 배열 기반의 PreparedStatementSetter 구현체
 * <p>
 * 가변 인자로 전달된 파라미터를 PreparedStatement에 순서대로 바인딩합니다.
 *
 * @see <a href="https://github.com/spring-projects/spring-framework/blob/main/spring-jdbc/src/main/java/org/springframework/jdbc/core/ArgumentPreparedStatementSetter.java">Spring Framework ArgumentPreparedStatementSetter</a>
 */
public class ArgumentPreparedStatementSetter implements PreparedStatementSetter {

    private final Object[] args;

    public ArgumentPreparedStatementSetter(Object[] args) {
        this.args = args;
    }

    @Override
    public void setValues(PreparedStatement pstmt) throws SQLException {
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
        }
    }
}
