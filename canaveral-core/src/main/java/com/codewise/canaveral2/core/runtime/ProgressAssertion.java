package com.codewise.canaveral2.core.runtime;

@FunctionalInterface
public interface ProgressAssertion {

    ProgressAssertion CAN_PROGRESS_ASSERTION = (context) -> true;

    boolean canProceed(RunnerContext runnerContext);
}
