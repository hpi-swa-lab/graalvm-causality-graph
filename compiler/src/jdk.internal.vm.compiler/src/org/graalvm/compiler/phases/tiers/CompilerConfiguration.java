/*
 * Copyright (c) 2013, 2022, Oracle and/or its affiliates. All rights reserved.
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
package org.graalvm.compiler.phases.tiers;

import org.graalvm.compiler.lir.phases.AllocationPhase.AllocationContext;
import org.graalvm.compiler.lir.phases.FinalCodeAnalysisPhase.FinalCodeAnalysisContext;
import org.graalvm.compiler.lir.phases.LIRPhaseSuite;
import org.graalvm.compiler.lir.phases.PostAllocationOptimizationPhase.PostAllocationOptimizationContext;
import org.graalvm.compiler.lir.phases.PreAllocationOptimizationPhase.PreAllocationOptimizationContext;
import org.graalvm.compiler.nodes.graphbuilderconf.GraphBuilderConfiguration.Plugins;
import org.graalvm.compiler.nodes.spi.Replacements;
import org.graalvm.compiler.options.OptionValues;
import org.graalvm.compiler.phases.PhaseSuite;

import jdk.vm.ci.code.Architecture;

public interface CompilerConfiguration {

    PhaseSuite<HighTierContext> createHighTier(OptionValues options);

    PhaseSuite<MidTierContext> createMidTier(OptionValues options);

    PhaseSuite<LowTierContext> createLowTier(OptionValues options, Architecture arch);

    LIRPhaseSuite<PreAllocationOptimizationContext> createPreAllocationOptimizationStage(OptionValues options);

    LIRPhaseSuite<AllocationContext> createAllocationStage(OptionValues options);

    LIRPhaseSuite<PostAllocationOptimizationContext> createPostAllocationOptimizationStage(OptionValues options);

    LIRPhaseSuite<FinalCodeAnalysisContext> createFinalCodeAnalysisStage(OptionValues options);

    @SuppressWarnings("unused")
    default void registerGraphBuilderPlugins(Architecture arch, Plugins plugins, OptionValues options, Replacements replacements) {
    }
}
