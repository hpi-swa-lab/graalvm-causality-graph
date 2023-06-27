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
package com.oracle.svm.hosted;

import com.oracle.graal.pointsto.meta.AnalysisMethod;
import com.oracle.graal.pointsto.meta.AnalysisType;
import com.oracle.graal.pointsto.reports.CausalityExport;
import com.oracle.svm.core.BuildArtifacts;
import com.oracle.svm.core.BuildArtifacts.ArtifactType;
import com.oracle.svm.core.SubstrateOptions;
import com.oracle.svm.core.feature.AutomaticallyRegisteredFeature;
import com.oracle.svm.core.feature.InternalFeature;
import com.oracle.svm.core.option.HostedOptionValues;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Stream;

@AutomaticallyRegisteredFeature
@SuppressWarnings("unused")
public class FlatReachabilityExporter implements InternalFeature {

    public final Path reachableTypesPath = NativeImageGenerator
            .generatedFiles(HostedOptionValues.singleton())
            .resolve("reachable_types.txt");

    public final Path instantiatedTypesPath = NativeImageGenerator
            .generatedFiles(HostedOptionValues.singleton())
            .resolve("instantiated_types.txt");

    public final Path reachableMethodsPath = NativeImageGenerator
            .generatedFiles(HostedOptionValues.singleton())
            .resolve("reachable_methods.txt");

    @Override
    public boolean isInConfiguration(IsInConfigurationAccess access) {
        return SubstrateOptions.GenerateFlatReachability.getValue();
    }

    @Override
    public void afterAnalysis(AfterAnalysisAccess access) {
        FeatureImpl.AfterAnalysisAccessImpl accessImpl = (FeatureImpl.AfterAnalysisAccessImpl) access;

        try (
                BufferedWriter reachableTypes = new BufferedWriter(new FileWriter(reachableTypesPath.toFile()));
                BufferedWriter instantiatedTypes = new BufferedWriter(new FileWriter(instantiatedTypesPath.toFile()))
        ) {
            ArrayList<AnalysisType> types = new ArrayList<>(accessImpl.getUniverse().getTypes());
            types.sort(Comparator.comparing(CausalityExport::stableTypeName));

            for (AnalysisType t : types) {
                if (t.isReachable()) {
                    reachableTypes.write(CausalityExport.stableTypeName(t));
                    reachableTypes.newLine();
                }
                if (t.isInstantiated()) {
                    instantiatedTypes.write(CausalityExport.stableTypeName(t));
                    instantiatedTypes.newLine();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BuildArtifacts.singleton().add(ArtifactType.BUILD_INFO, reachableTypesPath);
        BuildArtifacts.singleton().add(ArtifactType.BUILD_INFO, instantiatedTypesPath);

        try (BufferedWriter methods = new BufferedWriter(new FileWriter(reachableMethodsPath.toFile()))) {
            Stream<String> names = accessImpl
                    .getUniverse()
                    .getMethods()
                    .stream()
                    .filter(AnalysisMethod::isReachable)
                    .map(CausalityExport::stableMethodName)
                    .sorted();

            for(String name : (Iterable<String>) names::iterator) {
                methods.write(name);
                methods.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BuildArtifacts.singleton().add(ArtifactType.BUILD_INFO, reachableMethodsPath);
    }
}
