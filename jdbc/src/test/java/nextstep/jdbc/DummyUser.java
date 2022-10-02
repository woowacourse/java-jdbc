package nextstep.jdbc;

public class DummyUser {

    private Long id;
    private final String account;
    private String password;
    private final String email;

    public DummyUser(long id, String account, String password, String email) {
        this.id = id;
        this.account = account;
        this.password = password;
        this.email = email;
    }

    public DummyUser(String account, String password, String email) {
        this.account = account;
        this.password = password;
        this.email = email;
    }

}
