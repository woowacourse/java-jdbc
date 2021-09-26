package com.techcourse.dao;

import java.sql.PreparedStatement;

public interface PreparedStatementSetter {

    void setValues(PreparedStatement pstmt);
}
