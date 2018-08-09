package com.ffb.canaveral2.core.runtime;

public class RunnerInitializationException extends IllegalStateException {

    public RunnerInitializationException(Throwable cause) {
        super("Could not initialize runner.", cause);
    }
}
