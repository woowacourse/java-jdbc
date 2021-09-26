package nextstep.jdbc.datasource;

public class DataSourceProperty {
    private String url;
    private String username;
    private String password;

    public static class Builder {
        private String url;
        private String username;
        private String password;

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public DataSourceProperty build() {
            return new DataSourceProperty(this);
        }
    }

    public DataSourceProperty(Builder builder) {
        this.url = builder.url;
        this.username = builder.username;
        this.password = builder.password;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
