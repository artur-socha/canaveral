package com.ffb.canaveral2.core.mock;

public interface MockConfig<T> {

    String HOST = "localhost";

    T build(String mockName);
}
