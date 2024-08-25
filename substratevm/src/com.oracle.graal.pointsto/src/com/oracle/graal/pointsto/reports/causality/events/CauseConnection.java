package com.oracle.graal.pointsto.reports.causality.events;

public final class CauseConnection extends CausalityEvent {
    public final StackTraceElement from;
    public final StackTraceElement to;

    CauseConnection(StackTraceElement from, StackTraceElement to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public boolean essential() {
        return false;
    }

    @Override
    public EventKinds typeDescriptor() {
        return EventKinds.CauseConnection;
    }

    @Override
    public String toString() {
        return from + ";" + to + typeDescriptor().suffix;
    }
}
