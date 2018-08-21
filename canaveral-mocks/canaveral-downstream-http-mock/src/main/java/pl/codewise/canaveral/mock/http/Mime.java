package pl.codewise.canaveral.mock.http;

public enum Mime {
    JSON("application/json"),
    XML("application/xml"),
    X_WWW_FORM("application/x-www-form-urlencoded"),
    HTML("text/html"),
    TEXT("text/plain");

    private final String mime;

    Mime(String mime) {
        this.mime = mime;
    }

    public String getMime() {
        return mime;
    }
}
