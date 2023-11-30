package com.oracle.svm.hosted;

import java.io.FileWriter;
import java.io.IOException;

import org.graalvm.compiler.core.gen.LIRCompilerBackend;
import org.graalvm.compiler.options.Option;

import com.oracle.graal.pointsto.util.AnalysisError;
import com.oracle.svm.core.feature.AutomaticallyRegisteredFeature;
import com.oracle.svm.core.feature.InternalFeature;
import com.oracle.svm.core.option.HostedOptionKey;
import com.oracle.svm.core.option.HostedOptionValues;

import jdk.vm.ci.meta.ResolvedJavaMethod;

@AutomaticallyRegisteredFeature
public class UnsafeCoverageFeature implements InternalFeature {
    public static class Options {
        @Option(help = "Method coverage for x86_64.")//
        public static final HostedOptionKey<Boolean> UnsafeCoverage = new HostedOptionKey<Boolean>(false);
    }

    @Override
    public boolean isInConfiguration(IsInConfigurationAccess access) {
        boolean result = Options.UnsafeCoverage.getValue();
        if (result) {
            LIRCompilerBackend.coverageMethodsToId = this::getIdForMethods;
        }
        return result;
    }

    FileWriter writer;

    @Override
    public void beforeCompilation(BeforeCompilationAccess access) {
        try {
            writer = new FileWriter(NativeImageGenerator.generatedFiles(HostedOptionValues.singleton()).resolve("method_info.txt").toFile());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void afterCompilation(AfterCompilationAccess access) {
        try {
            writer.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            writer = null;
        }
    }

    int methodId = 0;

    private synchronized int assignId(String line) {
        int id = methodId++;
        try {
            writer.write(line);
        } catch (IOException | NullPointerException e) {
            throw AnalysisError.shouldNotReachHere(e);
        }
        return id;
    }

    private int getIdForMethods(Iterable<ResolvedJavaMethod> inlinedMethods) {
        StringBuilder sb = new StringBuilder();
        for (var m : inlinedMethods) {
            sb.append(m.format("%H.%n(%P):%R;"));
        }
        String name;
        if (sb.isEmpty()) {
            name = "\n";
        } else {
            sb.setCharAt(sb.length() - 1, '\n');
            name = sb.toString();
        }
        return assignId(name);
    }
}
