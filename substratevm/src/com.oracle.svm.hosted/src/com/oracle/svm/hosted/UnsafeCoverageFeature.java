package com.oracle.svm.hosted;

import java.io.FileWriter;
import java.io.IOException;

import org.graalvm.compiler.core.gen.LIRCompilerBackend;
import org.graalvm.compiler.options.Option;

import com.oracle.svm.core.feature.AutomaticallyRegisteredFeature;
import com.oracle.svm.core.feature.InternalFeature;
import com.oracle.svm.core.option.HostedOptionKey;
import com.oracle.svm.core.option.HostedOptionValues;

@AutomaticallyRegisteredFeature
public class UnsafeCoverageFeature implements InternalFeature {
    public static class Options {
        @Option(help = "Method coverage for x86_64.")//
        public static final HostedOptionKey<Boolean> UnsafeCoverage = new HostedOptionKey<Boolean>(false);
    }

    @Override
    public boolean isInConfiguration(IsInConfigurationAccess access) {
        return Options.UnsafeCoverage.getValue();
    }

    FileWriter writer;

    @Override
    public void beforeCompilation(BeforeCompilationAccess access) {
        try {
            writer = new FileWriter(NativeImageGenerator.generatedFiles(HostedOptionValues.singleton()).resolve("method_info.txt").toFile());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        LIRCompilerBackend.coverageMethodNameSink = methodName -> {
            try {
                writer.write(methodName);
                writer.write('\n');
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        };
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
}
