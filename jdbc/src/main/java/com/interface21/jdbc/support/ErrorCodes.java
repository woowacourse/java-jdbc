package com.interface21.jdbc.support;

import java.sql.SQLException;
import com.interface21.dao.DataAccessException;

public interface ErrorCodes {

    boolean contains(int code);

    DataAccessException translate(SQLException sqlException);
}
