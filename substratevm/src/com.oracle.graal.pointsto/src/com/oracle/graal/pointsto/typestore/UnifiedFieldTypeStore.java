/*
 * Copyright (c) 2015, 2022, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.graal.pointsto.typestore;

import com.oracle.graal.pointsto.flow.FieldTypeFlow;
import com.oracle.graal.pointsto.flow.context.object.AnalysisObject;
import com.oracle.graal.pointsto.meta.AnalysisField;

/**
 * Store for instance field access type flows. The read and write flows are unified.
 */
public class UnifiedFieldTypeStore extends FieldTypeStore {

    private final FieldTypeFlow readWriteFlow;

    public UnifiedFieldTypeStore(AnalysisField field, AnalysisObject object) {
        this(field, object, new FieldTypeFlow(field, field.getType(), object));
    }

    public UnifiedFieldTypeStore(AnalysisField field, AnalysisObject object, FieldTypeFlow fieldFlow) {
        super(field, object);
        this.readWriteFlow = fieldFlow;
    }

    @Override
    public FieldTypeFlow readFlow() {
        return readWriteFlow;
    }

    @Override
    public FieldTypeFlow writeFlow() {
        return readWriteFlow;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("UnifiedFieldStore<").append(field.format("%h.%n")).append(System.lineSeparator()).append(object).append(">");
        return str.toString();
    }

}
