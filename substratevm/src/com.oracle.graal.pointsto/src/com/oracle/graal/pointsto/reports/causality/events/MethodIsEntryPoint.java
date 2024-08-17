package com.oracle.graal.pointsto.reports.causality.events;

import com.oracle.graal.pointsto.meta.AnalysisMethod;

public class MethodIsEntryPoint extends AnalysisMethodEvent {
    public MethodIsEntryPoint(AnalysisMethod method) {
        super(method);
    }

    @Override
    public EventKinds typeDescriptor() {
        return EventKinds.MethodIsEntryPoint;
    }
}
