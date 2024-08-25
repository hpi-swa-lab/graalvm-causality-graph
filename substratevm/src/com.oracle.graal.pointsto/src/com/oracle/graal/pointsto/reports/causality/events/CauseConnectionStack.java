package com.oracle.graal.pointsto.reports.causality.events;

public final class CauseConnectionStack extends CausalityEvent {
    public final ImmutableStackTrace stackTrace;

    CauseConnectionStack(ImmutableStackTrace stackTrace) {
        this.stackTrace = stackTrace;
    }

    @Override
    public boolean essential() {
        return false;
    }

    @Override
    public EventKinds typeDescriptor() {
        return EventKinds.CauseConnectionStack;
    }

    @Override
    public String toString() {
        return stackTrace.toString() + typeDescriptor().suffix;
    }
}
