/*
 * Copyright (c) 2022, 2022, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.graal.pointsto.typestate;

import java.util.Collection;
import java.util.Collections;

import com.oracle.graal.pointsto.PointsToAnalysis;
import com.oracle.graal.pointsto.reports.CausalityExport;
import com.oracle.graal.pointsto.flow.AbstractSpecialInvokeTypeFlow;
import com.oracle.graal.pointsto.flow.ActualReturnTypeFlow;
import com.oracle.graal.pointsto.flow.MethodFlowsGraph;
import com.oracle.graal.pointsto.flow.TypeFlow;
import com.oracle.graal.pointsto.meta.AnalysisType;
import com.oracle.graal.pointsto.meta.PointsToAnalysisMethod;

import jdk.vm.ci.code.BytecodePosition;

final class DefaultSpecialInvokeTypeFlow extends AbstractSpecialInvokeTypeFlow {

    MethodFlowsGraph calleeFlows = null;

    DefaultSpecialInvokeTypeFlow(BytecodePosition invokeLocation, AnalysisType receiverType, PointsToAnalysisMethod targetMethod,
                    TypeFlow<?>[] actualParameters, ActualReturnTypeFlow actualReturn) {
        super(invokeLocation, receiverType, targetMethod, actualParameters, actualReturn);
    }

    @Override
    public void onObservedUpdate(PointsToAnalysis bb) {
        assert !isSaturated();
        /* The receiver state has changed. Process the invoke. */

        /*
         * If this is the first time the invoke is updated then set the callee and link the calee's
         * type flows. If this invoke is never updated then the callee will never be set, therefore
         * the callee will be unreachable from this call site.
         */
        initCallee();

        if(bb.getPurgeInfo().purgeRequested(callee.getMethod()))
            return;

        if (calleeFlows == null) {
            calleeFlows = callee.getOrCreateMethodFlowsGraph(bb, this);
            linkCallee(bb, false, calleeFlows);
            CausalityExport.getInstance().addDirectInvoke(this.method(), calleeFlows.getMethod());
        }

        /*
         * Every time the actual receiver state changes in the caller the formal receiver state
         * needs to be updated as there is no direct update link between actual and formal
         * receivers.
         */
        TypeState invokeState = filterReceiverState(bb, getReceiver().getState());
        updateReceiver(bb, calleeFlows, invokeState);
    }

    @Override
    public Collection<MethodFlowsGraph> getCalleesFlows(PointsToAnalysis bb) {
        if (callee == null) {
            /* This static invoke was not updated. */
            return Collections.emptyList();
        } else {
            return Collections.singletonList(callee.getMethodFlowsGraph());
        }
    }
}
