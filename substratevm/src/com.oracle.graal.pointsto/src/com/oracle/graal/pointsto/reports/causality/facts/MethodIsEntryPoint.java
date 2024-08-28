package com.oracle.graal.pointsto.reports.causality.facts;

import com.oracle.graal.pointsto.meta.AnalysisMethod;

public class MethodIsEntryPoint extends AnalysisMethodFact {
    public MethodIsEntryPoint(AnalysisMethod method) {
        super(method);
    }

    @Override
    public FactKinds typeDescriptor() {
        return FactKinds.MethodIsEntryPoint;
    }
}
