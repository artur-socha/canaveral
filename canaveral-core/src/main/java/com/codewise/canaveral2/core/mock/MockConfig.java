package com.codewise.canaveral2.core.mock;

public interface MockConfig<T> {

    String HOST = "localhost";

    T build(String mockName);
}
