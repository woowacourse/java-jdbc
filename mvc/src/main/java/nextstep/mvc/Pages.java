package nextstep.mvc;

public enum Pages {

    LOGIN("/login.jsp"),
    REGISTER("/register.jsp"),
    INDEX("/index.jsp"),
    UNAUTHORIZED("/401.jsp"),
    BAD_REQUEST("/404.jsp"),
    INTERNAL_SERVER("/500.jsp");

    private final String pageName;

    Pages(String pageName) {
        this.pageName = pageName;
    }

    public String getPageName() {
        return pageName;
    }

    public String redirectPageName() {
        return "redirect:" + this.getPageName();
    }
}
