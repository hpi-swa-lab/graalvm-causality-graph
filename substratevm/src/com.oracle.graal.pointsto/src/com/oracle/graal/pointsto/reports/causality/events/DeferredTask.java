package com.oracle.graal.pointsto.reports.causality.events;

import java.util.Objects;

public final class DeferredTask extends CausalityEvent {
    private final Object task;

    public DeferredTask(Object task) {
        this.task = task;
    }

    @Override
    public String toString() {
        return Objects.toIdentityString(task) + typeDescriptor().suffix;
    }

    @Override
    public boolean essential() {
        return false;
    }

    @Override
    public EventKinds typeDescriptor() {
        return EventKinds.DeferredTask;
    }
}
