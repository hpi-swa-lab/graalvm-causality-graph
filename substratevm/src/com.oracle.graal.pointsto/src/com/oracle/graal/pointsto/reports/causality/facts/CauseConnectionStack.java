package com.oracle.graal.pointsto.reports.causality.facts;

public final class CauseConnectionStack extends Fact {
    public final ImmutableStackTrace stackTrace;

    CauseConnectionStack(ImmutableStackTrace stackTrace) {
        this.stackTrace = stackTrace;
    }

    @Override
    public boolean essential() {
        return false;
    }

    @Override
    public FactKinds typeDescriptor() {
        return FactKinds.CauseConnectionStack;
    }

    @Override
    public String toString() {
        return stackTrace.toString() + typeDescriptor().suffix;
    }
}
