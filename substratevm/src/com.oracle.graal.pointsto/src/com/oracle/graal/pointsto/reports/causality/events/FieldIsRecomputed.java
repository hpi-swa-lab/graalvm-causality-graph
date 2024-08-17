package com.oracle.graal.pointsto.reports.causality.events;

import com.oracle.graal.pointsto.meta.AnalysisField;

public class FieldIsRecomputed extends AnalysisFieldEvent {
    FieldIsRecomputed(AnalysisField field) {
        super(field);
    }

    @Override
    public EventKinds typeDescriptor() {
        return EventKinds.FieldIsRecomputed;
    }
}
