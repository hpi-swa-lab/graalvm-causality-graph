/*
 * Copyright (c) 2023, 2023, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.graal.pointsto.reports.causality;

import java.lang.reflect.Field;

public class HeapAssignmentTracing {
    private static final HeapAssignmentTracing instance;

    static {
        HeapAssignmentTracing impl = new NativeImpl();
        try {
            // Try to invoke native method
            impl.getResponsibleClass(new Object());
        } catch (UnsatisfiedLinkError error) {
            // JVMTI agent is not loaded
            impl = new HeapAssignmentTracing();
        }
        instance = impl;
    }

    public static HeapAssignmentTracing getInstance() {
        return instance;
    }

    public static boolean isActive() {
        return instance instanceof NativeImpl;
    }

    public Object getResponsibleClass(Object imageHeapObject) {
        return null;
    }

    public Object getClassResponsibleForNonstaticFieldWrite(Object receiver, Field field, Object val) {
        return null;
    }

    public Object getClassResponsibleForStaticFieldWrite(Class<?> declaring, Field field, Object val) {
        return null;
    }

    public Object getClassResponsibleForArrayWrite(Object[] array, int index, Object val) {
        return null;
    }

    public Object getBuildTimeClinitResponsibleForBuildTimeClinit(Class<?> clazz) {
        return null;
    }

    public void setCause(Object cause, boolean recordHeapAssignments) {
    }

    public void dispose() {
    }

    private static final class NativeImpl extends HeapAssignmentTracing {
        @Override
        public native Object getResponsibleClass(Object imageHeapObject);

        @Override
        public native Object getClassResponsibleForNonstaticFieldWrite(Object receiver, Field field, Object val);

        @Override
        public native Object getClassResponsibleForStaticFieldWrite(Class<?> declaring, Field field, Object val);

        @Override
        public native Object getClassResponsibleForArrayWrite(Object[] array, int index, Object val);

        @Override
        public native Object getBuildTimeClinitResponsibleForBuildTimeClinit(Class<?> clazz);

        @Override
        public native void setCause(Object cause, boolean recordHeapAssignments);

        @Override
        public native void dispose();
    }
}
