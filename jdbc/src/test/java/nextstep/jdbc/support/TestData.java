package nextstep.jdbc.support;

public class TestData {

    private Long id;
    private String content;
    private int num;

    public TestData() {
    }

    public TestData(final String content, final int num) {
        this.content = content;
        this.num = num;
    }

    public TestData(final Long id, final String content, final int num) {
        this.id = id;
        this.content = content;
        this.num = num;
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public int getNum() {
        return num;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public void setNum(int num) {
        this.num = num;
    }
}
