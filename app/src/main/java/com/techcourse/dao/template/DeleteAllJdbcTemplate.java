package com.techcourse.dao.template;

import com.interface21.jdbc.core.JdbcTemplate;
import java.util.Map;
import javax.sql.DataSource;

public class DeleteAllJdbcTemplate extends JdbcTemplate<Void> {

    private final DataSource dataSource;

    public DeleteAllJdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    protected String createQuery() {
        return "DELETE FROM users";
    }

    @Override
    protected void setValues(final Void object, final Map<String, Object> params) {
        // 파라미터가 필요 없으므로 비워둠
    }

    @Override
    protected DataSource getDataSource() {
        return dataSource;
    }
}
