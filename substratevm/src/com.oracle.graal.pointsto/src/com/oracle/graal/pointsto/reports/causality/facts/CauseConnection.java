package com.oracle.graal.pointsto.reports.causality.facts;

public final class CauseConnection extends Fact {
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
    public FactKinds typeDescriptor() {
        return FactKinds.CauseConnection;
    }

    @Override
    public String toString() {
        return from + ";" + to + typeDescriptor().suffix;
    }
}
