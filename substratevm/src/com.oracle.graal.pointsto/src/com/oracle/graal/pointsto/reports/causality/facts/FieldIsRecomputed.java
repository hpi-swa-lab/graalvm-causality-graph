package com.oracle.graal.pointsto.reports.causality.facts;

import com.oracle.graal.pointsto.meta.AnalysisField;

public class FieldIsRecomputed extends AnalysisFieldFact {
    FieldIsRecomputed(AnalysisField field) {
        super(field);
    }

    @Override
    public FactKinds typeDescriptor() {
        return FactKinds.FieldIsRecomputed;
    }
}
