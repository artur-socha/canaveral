package pl.codewise.canaveral.core.mock;

public interface MockConfig<T> {

    String HOST = "localhost";

    T build(String mockName);
}
