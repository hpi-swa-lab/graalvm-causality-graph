package com.oracle.graal.pointsto.reports.causality.facts;

import java.util.Objects;

public final class DeferredTask extends Fact {
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
    public FactKinds typeDescriptor() {
        return FactKinds.DeferredTask;
    }
}
