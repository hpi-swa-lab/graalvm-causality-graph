package com.oracle.svm.hosted;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;

import org.graalvm.compiler.bytecode.Bytecodes;
import org.graalvm.compiler.debug.DebugContext;
import org.graalvm.compiler.nodes.StructuredGraph;

import com.oracle.graal.pointsto.meta.AnalysisType;
import com.oracle.graal.pointsto.meta.AnalysisUniverse;
import com.oracle.graal.pointsto.meta.HostedProviders;
import com.oracle.svm.core.SubstrateOptions;
import com.oracle.svm.core.annotate.Delete;
import com.oracle.svm.core.feature.AutomaticallyRegisteredFeature;
import com.oracle.svm.core.feature.InternalFeature;
import com.oracle.svm.core.util.VMError;
import com.oracle.svm.hosted.annotation.CustomSubstitutionMethod;
import com.oracle.svm.hosted.substitute.DeletedMethod;

import jdk.vm.ci.meta.ResolvedJavaMethod;

@AutomaticallyRegisteredFeature
@SuppressWarnings("unused")
public class MethodEviscerationFeature implements InternalFeature {
    private static HashSet<String> readLines(String path) {
        HashSet<String> lines = new HashSet<>();
        try (BufferedReader r = new BufferedReader(new FileReader(SubstrateOptions.EviscerateMethodsPath.getValue()))) {
            for(String line; (line = r.readLine()) != null;) {
                lines.add(line);
            }
        } catch (IOException e) {
            throw VMError.shouldNotReachHere(e);
        }
        return lines;
    }

    @Override
    public boolean isInConfiguration(IsInConfigurationAccess access) {
        return SubstrateOptions.EviscerateMethodsPath.getValue() != null;
    }

    @Override
    public void duringSetup(DuringSetupAccess access) {
        var impl = (FeatureImpl.DuringSetupAccessImpl) access;
        impl.registerSubstitutionProcessor(new SubstitutionProcessor(impl.getBigBang().getUniverse(), readLines(SubstrateOptions.EviscerateMethodsPath.getValue())));
    }

    private static class SubstitutionProcessor extends com.oracle.graal.pointsto.infrastructure.SubstitutionProcessor {
        private final AnalysisUniverse universe;
        private final HashSet<String> victimNames;
        private final HashMap<ResolvedJavaMethod, CustomSubstitutionMethod> cache = new HashMap<>();

        public SubstitutionProcessor(AnalysisUniverse universe, HashSet<String> victimNames) {
            this.universe = universe;
            this.victimNames = victimNames;
        }

        public ResolvedJavaMethod lookup(ResolvedJavaMethod method) {
            CustomSubstitutionMethod subst = cache.get(method);
            if (subst != null)
                return subst;

            String name;
            try {
                AnalysisType type = universe.lookup(method.getDeclaringClass());
                name = FlatReachabilityExporter.stableTypeName(type) + '.' + method.format("%n(%P):%R");
            } catch(NullPointerException ex) {
                // Some CFunction-Stuff can't handle Signature the right way and throws on JavaMethod.format("%H.%n(%P)");
                return method;
            }

            if (!victimNames.contains(name)) {
                return method;
            }

            if (SubstrateOptions.EviscerateMethodsWithExceptions.getValue()) {
                subst = new DeletedMethod(method, new Delete() {
                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return Delete.class;
                    }

                    @Override
                    public String value() {
                        return "Manually eviscerated for Debloating";
                    }
                });
            } else {
                subst = new EvisceratedMethod(method);
            }

            cache.put(method, subst);
            return subst;
        }

        public ResolvedJavaMethod resolve(ResolvedJavaMethod method) {
            return method instanceof CustomSubstitutionMethod em ? em.getOriginal() : method;
        }
    }

    private static class EvisceratedMethod extends CustomSubstitutionMethod {
        public EvisceratedMethod(ResolvedJavaMethod original) {
            super(original);
        }

        @Override
        public StructuredGraph buildGraph(DebugContext debug, ResolvedJavaMethod method, HostedProviders providers, Purpose purpose) {
            return null;
        }

        @Override
        public byte[] getCode() {
            return switch(original.getSignature().getReturnKind()) {
                case Boolean, Byte, Short, Char, Int -> new byte[] { Bytecodes.ICONST_0, (byte)Bytecodes.IRETURN };
                case Long -> new byte[] { Bytecodes.LCONST_0, (byte)Bytecodes.LRETURN };
                case Float -> new byte[] { Bytecodes.FCONST_0, (byte)Bytecodes.FRETURN };
                case Double -> new byte[] { Bytecodes.DCONST_0, (byte)Bytecodes.DRETURN };
                case Object -> new byte[] { Bytecodes.ACONST_NULL, (byte)Bytecodes.ARETURN };
                case Void -> new byte[] { (byte)Bytecodes.RETURN };
                case Illegal -> null;
            };
        }

        @Override
        public int getCodeSize() {
            byte[] code = getCode();
            return code == null ? 0 : code.length;
        }
    }
}
