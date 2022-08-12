/**
 * Copyright (c) 2022, 2022 University of Sao Paulo and Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matheus Soares - initial API and implementation and/or initial documentation
 */
package br.usp.each.saeg.badua.core.analysis;

import br.usp.each.saeg.asm.defuse.*;
import br.usp.each.saeg.badua.core.data.ExecutionData;
import br.usp.each.saeg.badua.core.internal.instr.InstrSupport;
import br.usp.each.saeg.badua.core.util.Edge;
import org.jacoco.core.internal.analysis.StringPool;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import java.util.*;

import static br.usp.each.saeg.commons.BitSetUtils.valueOf;

public class ClassAnalyzer extends ClassVisitor {

    private final ExecutionData execData;

    private final StringPool stringPool;

    private ClassCoverage coverage;

    private boolean interfaceType;

    private int window;

    private boolean edgeCoverage;

    public ClassAnalyzer(final ExecutionData execData, final StringPool stringPool, final boolean edgeCoverage) {
        super(Opcodes.ASM9);
        this.execData = execData;
        this.stringPool = stringPool;
        this.edgeCoverage = edgeCoverage;
    }

    @Override
    public void visit(final int version,
                      final int access,
                      final String name,
                      final String signature,
                      final String superName,
                      final String[] interfaces) {

        if (!execData.getName().equals(name)) {
            throw new IllegalStateException("The provided data is incompatible.");
        }
        coverage = new ClassCoverage(stringPool.get(name));
        interfaceType = (access & Opcodes.ACC_INTERFACE) != 0;
        window = 0;
    }

    @Override
    public FieldVisitor visitField(final int access,
                                   final String name,
                                   final String desc,
                                   final String signature,
                                   final Object value) {

        InstrSupport.assertNotInstrumented(name, coverage.getName());
        return null;
    }

    @Override
    public MethodVisitor visitMethod(final int access,
                                     final String name,
                                     final String desc,
                                     final String signature,
                                     final String[] exceptions) {

        InstrSupport.assertNotInstrumented(name, coverage.getName());

        // Does not analyze:
        // 1. Interfaces
        if (interfaceType)
            return null;
        // 2. Abstract methods
        else if ((access & Opcodes.ACC_ABSTRACT) != 0)
            return null;
        // 3. Static class initialization
        else if (name.equals("<clinit>"))
            return null;

        return new MethodNode(Opcodes.ASM9, access, name, desc, signature, exceptions) {

            @Override
            public void visitEnd() {

                final DefUseInterpreter interpreter = new DefUseInterpreter();
                final FlowAnalyzer<Value> flowAnalyzer = new FlowAnalyzer<Value>(interpreter);
                final DefUseAnalyzer analyzer = new DefUseAnalyzer(flowAnalyzer, interpreter);
                try {
                    analyzer.analyze(coverage.getName(), this);
                } catch (final AnalyzerException e) {
                    throw new RuntimeException(e);
                }

                // Instructions by line number
                final int[] lines = getLines();

                final ArrayList<Edge> edges = Edge.getEdges(flowAnalyzer.getSuccessors(), flowAnalyzer.getLeaders());

                final BitSet nodeData;

                final MethodCoverage methodCoverage = new MethodCoverage(name, desc);

                if (edgeCoverage) {
                    nodeData = getData(execData.getData(), edges.size());
                    edgeReport(flowAnalyzer, edges, nodeData, methodCoverage);
                } else {
                    nodeData = getData(execData.getData(), flowAnalyzer.getBasicBlocks().length);
                    nodeReport(lines, flowAnalyzer, nodeData, methodCoverage);
                }
            }

            public BitSet getData(final long[] raw, final int length) {
                if (raw != null) {
                    return valueOf(Arrays.copyOfRange(raw, window, incrementWindow(length)));
                }
                return new BitSet();
            }

            public int[] getLines() {
                final int[] lines = new int[instructions.size()];
                Arrays.fill(lines, MethodCoverage.UNKNOWN_LINE);
                for (int i = 0; i < instructions.size(); i++) {
                    if (instructions.get(i).getType() == AbstractInsnNode.LINE) {
                        final LineNumberNode line = (LineNumberNode) instructions.get(i);
                        Arrays.fill(lines, instructions.indexOf(line.start), lines.length, line.line);
                    }
                }
                return lines;
            }

        };
    }

    private void nodeReport(final int[] lines, final FlowAnalyzer<Value> flowAnalyzer, final BitSet nodeData, final MethodCoverage methodCoverage) {
        final int[][] basicBlocks = flowAnalyzer.getBasicBlocks();
        for (int b = 0; b < basicBlocks.length; b++) {
            final boolean coveredNode = nodeData.get(b);

            int[] coveredInstructions = basicBlocks[b];
            Collection<Integer> coveredLines = new TreeSet<Integer>();
            for (int i = 0; i < coveredInstructions.length; i++) {
                coveredLines.add(lines[coveredInstructions[i]]);
            }
            methodCoverage.increment(coveredNode, coveredLines);
        }

        if (methodCoverage.getCounter().getTotalCount() > 0) {
            coverage.addMethod(methodCoverage);
        }
    }

    private void edgeReport(final FlowAnalyzer<Value> flowAnalyzer, List<Edge> edges, final BitSet nodeData, final MethodCoverage methodCoverage) {
        final int[][] basicBlocks = flowAnalyzer.getBasicBlocks();


        for (int e = 0; e < edges.size(); e++) {
            final boolean coveredEdge = nodeData.get(e);

            methodCoverage.increment(e, coveredEdge, edges);
        }

        if (methodCoverage.getCounter().getTotalCount() > 0) {
            coverage.addMethod(methodCoverage);
        }
    }

    private int incrementWindow(final int n) {
        return window += (n + 63) / 64;
        //return window += n / 64;
    }

    public ClassCoverage getCoverage() {
        return coverage;
    }

}
