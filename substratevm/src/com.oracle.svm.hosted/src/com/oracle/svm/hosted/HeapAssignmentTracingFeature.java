package com.oracle.svm.hosted;

import com.oracle.graal.pointsto.reports.AnalysisReportsOptions;
import com.oracle.graal.pointsto.reports.HeapAssignmentTracing;
import com.oracle.svm.core.feature.AutomaticallyRegisteredFeature;
import com.oracle.svm.core.feature.InternalFeature;
import com.oracle.svm.core.option.HostedOptionValues;
import jdk.vm.ci.meta.ResolvedJavaMethod;
import org.graalvm.compiler.options.OptionValues;
import org.graalvm.nativeimage.hosted.Feature;

import java.util.ArrayList;
import java.util.List;

@AutomaticallyRegisteredFeature
public class HeapAssignmentTracingFeature implements InternalFeature {
    @Override
    public List<Class<? extends Feature>> getRequiredFeatures() {
        // Add CausalityExporter as a dependency such that this onAnalysisExit()
        // is called after that of the CausalityExporter
        ArrayList<Class<? extends Feature>> a = new ArrayList<>();
        a.add(CausalityExporter.class);
        return a;
    }

    @Override
    public boolean isInConfiguration(IsInConfigurationAccess access) {
        OptionValues options = HostedOptionValues.singleton();
        Object heapAssignmentTracingAgentValue = AnalysisReportsOptions.HeapAssignmentTracingAgent.getValue(options);
        return heapAssignmentTracingAgentValue == null && AnalysisReportsOptions.PrintCausalityGraph.getValue(options)
                || heapAssignmentTracingAgentValue == Boolean.TRUE;
    }

    @Override
    public void afterRegistration(AfterRegistrationAccess access) {
        HeapAssignmentTracing.activate();
    }

    @Override
    public void duringSetup(DuringSetupAccess access) {
        var impl = (FeatureImpl.DuringSetupAccessImpl) access;
        // Be careful not to trigger lookup of Object.<init>() into the AnalysisUniverse
        ResolvedJavaMethod constructor = impl.bb.getUniverse().objectType().getWrapped().getDeclaredConstructors(false)[0];
        impl.registerSubstitutionProcessor(new ObjectConstructorSubstitutionProcessor(constructor));
    }

    @Override
    public void onAnalysisExit(OnAnalysisExitAccess access) {
        HeapAssignmentTracing.getInstance().dispose();
    }

    /**
     * Is necessary to hide the instrumented {@link Object#Object()} from the analysis
     */
    private static class ObjectConstructorSubstitutionProcessor extends com.oracle.graal.pointsto.infrastructure.SubstitutionProcessor {
        public final ResolvedJavaMethod original;
        public final MethodEviscerationFeature.EvisceratedMethod substitution;

        public ObjectConstructorSubstitutionProcessor(ResolvedJavaMethod original) {
            this.original = original;
            this.substitution = new MethodEviscerationFeature.EvisceratedMethod(original);
        }

        public ResolvedJavaMethod lookup(ResolvedJavaMethod method) {
            return original.equals(method) ? substitution : method;
        }

        public ResolvedJavaMethod resolve(ResolvedJavaMethod method) {
            return method == substitution ? original : method;
        }
    }
}