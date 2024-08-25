package com.oracle.graal.pointsto.reports.causality.events;

import java.util.Arrays;

public record ImmutableStackTrace(StackTraceElement[] stackTrace) {
    @Override
    public boolean equals(Object obj) {
        return obj instanceof ImmutableStackTrace other && Arrays.equals(stackTrace, other.stackTrace);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(stackTrace);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : stackTrace) {
            sb.append(element).append(";");
        }
        return sb.toString();
    }
}
