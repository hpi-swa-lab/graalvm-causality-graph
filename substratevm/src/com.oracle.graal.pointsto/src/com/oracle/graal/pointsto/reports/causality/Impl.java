package com.oracle.graal.pointsto.reports.causality;

import com.oracle.graal.pointsto.PointsToAnalysis;
import com.oracle.graal.pointsto.flow.AbstractVirtualInvokeTypeFlow;
import com.oracle.graal.pointsto.flow.ActualParameterTypeFlow;
import com.oracle.graal.pointsto.flow.ActualReturnTypeFlow;
import com.oracle.graal.pointsto.flow.AllInstantiatedTypeFlow;
import com.oracle.graal.pointsto.flow.InvokeTypeFlow;
import com.oracle.graal.pointsto.flow.MethodTypeFlow;
import com.oracle.graal.pointsto.flow.TypeFlow;
import com.oracle.graal.pointsto.heap.ImageHeapConstant;
import com.oracle.graal.pointsto.meta.AnalysisElement;
import com.oracle.graal.pointsto.meta.AnalysisField;
import com.oracle.graal.pointsto.meta.AnalysisMethod;
import com.oracle.graal.pointsto.meta.AnalysisType;
import com.oracle.graal.pointsto.reports.CausalityExport;
import com.oracle.graal.pointsto.reports.HeapAssignmentTracing;
import com.oracle.graal.pointsto.typestate.TypeState;
import jdk.vm.ci.meta.JavaConstant;
import org.graalvm.collections.Pair;
import org.graalvm.nativeimage.hosted.Feature;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class Impl extends CausalityExport {
    private final HashSet<Pair<TypeFlow<?>, TypeFlow<?>>> interflows = new HashSet<>();
    private final HashSet<Pair<Reason, Reason>> direct_edges = new HashSet<>();
    private final HashMap<AnalysisMethod, Pair<Set<AbstractVirtualInvokeTypeFlow>, TypeState>> virtual_invokes = new HashMap<>();
    private final HashMap<TypeFlow<?>, AnalysisMethod> typeflowGateMethods = new HashMap<>();

    private final HashMap<InvokeTypeFlow, TypeFlow<?>> originalInvokeReceivers = new HashMap<>();
    private final HashMap<Pair<Reason, TypeFlow<?>>, TypeState> flowingFromHeap = new HashMap<>();

    public Impl() {
    }

    private static <K, V> void mergeMap(Map<K, V> dst, Map<K, V> src, BiFunction<V, V, V> merger) {
        src.forEach((k, v) -> dst.merge(k, v, merger));
    }

    private static <K> void mergeTypeFlowMap(Map<K, TypeState> dst, Map<K, TypeState> src, PointsToAnalysis bb) {
        mergeMap(dst, src, (v1, v2) -> TypeState.forUnion(bb, v1, v2));
    }

    public Impl(List<Impl> instances, PointsToAnalysis bb) {
        for (Impl i : instances) {
            interflows.addAll(i.interflows);
            direct_edges.addAll(i.direct_edges);
            mergeMap(virtual_invokes, i.virtual_invokes, (p1, p2) -> {
                p1.getLeft().addAll(p2.getLeft());
                return Pair.create(p1.getLeft(), TypeState.forUnion(bb, p1.getRight(), p2.getRight()));
            });
            typeflowGateMethods.putAll(i.typeflowGateMethods);
            originalInvokeReceivers.putAll(i.originalInvokeReceivers);
            mergeTypeFlowMap(flowingFromHeap, i.flowingFromHeap, bb);
        }
    }

    @Override
    public void addTypeFlowEdge(TypeFlow<?> from, TypeFlow<?> to) {
        if (from == to)
            return;

        if (currentlySaturatingDepth > 0)
            if (from instanceof AllInstantiatedTypeFlow)
                return;
            else
                assert to.isContextInsensitive() || from instanceof ActualReturnTypeFlow && to instanceof ActualReturnTypeFlow || to instanceof ActualParameterTypeFlow;

        interflows.add(Pair.create(from, to));
    }

    int currentlySaturatingDepth; // Inhibits the registration of new typeflow edges

    @Override
    public void beginSaturationHappening() {
        currentlySaturatingDepth++;
    }

    @Override
    public void endSaturationHappening() {
        currentlySaturatingDepth--;
        if (currentlySaturatingDepth < 0)
            throw new RuntimeException();
    }

    private static <T> T asObject(PointsToAnalysis bb, Class<T> tClass, JavaConstant constant) {
        if(constant instanceof ImageHeapConstant)
        {
            constant = ((ImageHeapConstant)constant).getHostedObject();
        }
        return bb.getSnippetReflectionProvider().asObject(tClass, constant);
    }

    @Override
    public void registerTypesFlowing(PointsToAnalysis bb, Reason reason, TypeFlow<?> destination, TypeState types) {
        flowingFromHeap.compute(Pair.create(reason, destination), (p, state) -> state == null ? types : TypeState.forUnion(bb, state, types));
    }

    @Override
    public void register(Reason reason, Reason consequence) {
        if((reason == null || reason.root()) && !rootReasons.empty())
            reason = rootReasons.peek();

        direct_edges.add(Pair.create(reason, consequence));
    }

    @Override
    public void addVirtualInvoke(PointsToAnalysis bb, AbstractVirtualInvokeTypeFlow invocation, AnalysisMethod concreteTargetMethod, TypeState concreteTargetMethodCallingTypes) {
        virtual_invokes.compute(concreteTargetMethod, (m, p) -> {
            if (p == null) {
                HashSet<AbstractVirtualInvokeTypeFlow> invocations = new HashSet<>(1);
                invocations.add(invocation);
                return Pair.create(invocations, concreteTargetMethodCallingTypes);
            } else {
                p.getLeft().add(invocation);
                return Pair.create(p.getLeft(), TypeState.forUnion(bb, p.getRight(), concreteTargetMethodCallingTypes));
            }
        });
    }

    @Override
    public void registerMethodFlow(MethodTypeFlow method) {
        for (TypeFlow<?> flow : method.getMethodFlowsGraph().flows()) {
            if (method.getMethod() == null)
                throw new RuntimeException("Null method registered");
            if (flow.method() != null)
                typeflowGateMethods.put(flow, method.getMethod());
        }
    }

    @Override
    public void registerVirtualInvokeTypeFlow(AbstractVirtualInvokeTypeFlow invocation) {
        originalInvokeReceivers.put(invocation, invocation.getReceiver());
    }

    private Reason getResponsibleClassReason(Class<?> responsible, Object o) {
        return responsible != null ? new BuildTimeClassInitialization(responsible) : new UnknownHeapObject(o.getClass());
    }

    @Override
    public Reason getReasonForHeapObject(PointsToAnalysis bb, JavaConstant heapObject) {
        Object o = bb.getSnippetReflectionProvider().asObject(Object.class, heapObject);
        Class<?> responsible = HeapAssignmentTracing.getInstance().getResponsibleClass(o);
        return getResponsibleClassReason(responsible, o);
    }

    @Override
    public Reason getReasonForHeapFieldAssignment(PointsToAnalysis bb, JavaConstant receiver, AnalysisField field, JavaConstant value) {
        Class<?> responsible;
        Object o = asObject(bb, Object.class, value);

        if(field.isStatic()) {
            responsible = HeapAssignmentTracing.getInstance().getClassResponsibleForStaticFieldWrite(field.getDeclaringClass().getJavaClass(), field.getJavaField(), o);
        } else {
            responsible = HeapAssignmentTracing.getInstance().getClassResponsibleForNonstaticFieldWrite(asObject(bb, Object.class, receiver), field.getJavaField(), o);
        }

        return getResponsibleClassReason(responsible, o);
    }

    @Override
    public Reason getReasonForHeapArrayAssignment(PointsToAnalysis bb, JavaConstant array, int elementIndex, JavaConstant value) {
        Object o = asObject(bb, Object.class, value);
        Class<?> responsible = HeapAssignmentTracing.getInstance().getClassResponsibleForArrayWrite(asObject(bb, Object[].class, array), elementIndex, o);
        return getResponsibleClassReason(responsible, o);
    }

    @Override
    public void registerReasonRoot(Reason reason) {
        register(null, reason);
    }

    private final Stack<Reason> rootReasons = new Stack<>();

    @Override
    protected void beginAccountingRootRegistrationsTo(Reason reason) {
        if(!rootReasons.empty() && reason != null && rootReasons.peek() != null && !rootReasons.peek().equals(reason) && reason != Ignored.Instance && rootReasons.peek() != Ignored.Instance && !(rootReasons.peek() instanceof Feature) && !rootReasons.peek().root())
            throw new RuntimeException("Stacking Rerooting requests!");

        rootReasons.push(reason);
    }

    @Override
    protected void endAccountingRootRegistrationsTo(Reason reason) {
        if(rootReasons.empty() || rootReasons.pop() != reason) {
            throw new RuntimeException("Invalid Call to endAccountingRootRegistrationsTo()");
        }
    }

    public Graph createCausalityGraph(PointsToAnalysis bb) {
        Graph g = new Graph();

        HashMap<TypeFlow<?>, Graph.RealFlowNode> flowMapping = new HashMap<>();

        Function<TypeFlow<?>, Graph.RealFlowNode> flowMapper = flow ->
        {
            if(flow.getState().typesCount() == 0 && !flow.isSaturated())
                return null;

            return flowMapping.computeIfAbsent(flow, f -> {
                AnalysisMethod m = typeflowGateMethods.get(f);

                MethodReachableReason reason = m == null ? null : new MethodReachableReason(m);

                if(reason != null && reason.unused())
                    return null;

                return Graph.RealFlowNode.create(bb, f, reason);
            });
        };

        direct_edges.removeIf(pair -> pair.getRight() instanceof MethodReachableReason && ((MethodReachableReason)pair.getRight()).element.isClassInitializer());

        for (Pair<Reason, Reason> e : direct_edges) {
            Reason from = e.getLeft();

            if(from != null && !from.unused() && from.root())
                g.directInvokes.add(new Graph.DirectCallEdge(null, from));
        }

        for (Pair<Reason, Reason> e : direct_edges) {
            Reason from = e.getLeft();
            Reason to = e.getRight();

            if(from != null && from.unused())
                continue;

            if(to.unused())
                continue;

            g.directInvokes.add(new Graph.DirectCallEdge(from, to));
        }

        for (Map.Entry<AnalysisMethod, Pair<Set<AbstractVirtualInvokeTypeFlow>, TypeState>> e : virtual_invokes.entrySet()) {

            MethodReachableReason reason = new MethodReachableReason(e.getKey());

            if(reason.unused())
                continue;

            Graph.InvocationFlowNode invocationFlowNode = new Graph.InvocationFlowNode(reason, e.getValue().getRight());

            e.getValue().getLeft().stream()
                    .map(originalInvokeReceivers::get)
                    .filter(Objects::nonNull)
                    .map(flowMapper::apply)
                    .filter(Objects::nonNull)
                    .map(receiverNode -> new Graph.FlowEdge(receiverNode, invocationFlowNode))
                    .forEach(g.interflows::add);
        }

        for (Pair<TypeFlow<?>, TypeFlow<?>> e : interflows) {
            Graph.RealFlowNode left = null;

            if(e.getLeft() != null) {
                left = flowMapper.apply(e.getLeft());
                if(left == null)
                    continue;
            }

            Graph.RealFlowNode right = flowMapper.apply(e.getRight());
            if(right == null)
                continue;

            g.interflows.add(new Graph.FlowEdge(left, right));
        }

        for (AnalysisType t : bb.getUniverse().getTypes()) {
            if (!t.isReachable())
                continue;

            AnalysisMethod classInitializer = t.getClassInitializer();
            if(classInitializer != null && classInitializer.isImplementationInvoked()) {
                g.directInvokes.add(new Graph.DirectCallEdge(new TypeReachableReason(t), new MethodReachableReason(classInitializer)));
            }
        }

        for(Pair<Reason, Reason> e : direct_edges) {
            if(e.getRight() instanceof TypeInstantiatedReason) {
                TypeInstantiatedReason reason = (TypeInstantiatedReason) e.getRight();

                if(!reason.unused()) {
                    AnalysisType t = reason.type;
                    TypeState state = TypeState.forExactType(bb, t, false);

                    String debugStr = "Virtual Flow Node for reaching " + t.toJavaName();

                    Graph.FlowNode vfn = new Graph.FlowNode(debugStr, reason, state);
                    g.interflows.add(new Graph.FlowEdge(null, vfn));

                    t.forAllSuperTypes(t1 -> {
                        g.interflows.add(new Graph.FlowEdge(vfn, flowMapper.apply(t1.instantiatedTypes)));
                        g.interflows.add(new Graph.FlowEdge(vfn, flowMapper.apply(t1.instantiatedTypesNonNull)));
                    });
                }
            }

            if(e.getLeft() instanceof BuildTimeClassInitialization) {
                BuildTimeClassInitialization init = (BuildTimeClassInitialization) e.getLeft();

                Optional<AnalysisType> optT = bb.getMetaAccess().optionalLookupJavaType(init.clazz);

                if(optT.isPresent()) {
                    TypeReachableReason tReachable = new TypeReachableReason(optT.get());

                    if(!tReachable.unused()) {
                        g.directInvokes.add(new Graph.DirectCallEdge(new TypeReachableReason(optT.get()), init));
                    }
                } else {
                    g.directInvokes.add(new Graph.DirectCallEdge(null, init));
                    System.err.println("Could not lookup type: " + init.clazz.getTypeName());
                }
            }
        }

        for (Map.Entry<Pair<Reason, TypeFlow<?>>, TypeState> e : flowingFromHeap.entrySet()) {
            Graph.RealFlowNode fieldNode = flowMapper.apply(e.getKey().getRight());

            if (fieldNode == null)
                continue;

            Graph.FlowNode intermediate = new Graph.FlowNode("Virtual Flow from Heap: " + e.getValue(), e.getKey().getLeft(), e.getValue());
            g.interflows.add(new Graph.FlowEdge(null, intermediate));
            g.interflows.add(new Graph.FlowEdge(intermediate, fieldNode));
        }

        return g;
    }
}
