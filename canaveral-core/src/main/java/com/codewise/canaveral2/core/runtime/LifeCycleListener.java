package com.codewise.canaveral2.core.runtime;

@SuppressWarnings("unused")
public interface LifeCycleListener {

    default int getPriority()
    {
        return 1;
    }

    default void afterAllMocksCreated(RunnerContext runnerContext)
    {

    }
}
