package nextstep.datasource;

public class DataSourceProperties {

    private String url;
    private String user;
    private String password;

    public String url() {
        return url;
    }

    public void url(String url) {
        this.url = url;
    }

    public String user() {
        return user;
    }

    public void user(String user) {
        this.user = user;
    }

    public String password() {
        return password;
    }

    public void password(String password) {
        this.password = password;
    }
}
