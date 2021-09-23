package exception;

import java.sql.SQLException;

public class DataAccessException extends RuntimeException{
    public DataAccessException(SQLException e) {
        super(e.getMessage());
    }

    public DataAccessException(String message) {
        super(message);
    }
}
