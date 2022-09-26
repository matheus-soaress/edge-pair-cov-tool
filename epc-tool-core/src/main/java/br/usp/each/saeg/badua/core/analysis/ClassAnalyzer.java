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
import br.usp.each.saeg.badua.core.util.GraphwizGenerator;
import org.jacoco.core.internal.analysis.StringPool;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static br.usp.each.saeg.commons.BitSetUtils.valueOf;

public class ClassAnalyzer extends ClassVisitor {

    private final ExecutionData execData;

    private final StringPool stringPool;

    private ClassCoverage coverage;

    private boolean interfaceType;

    private int window;

    private boolean edgeCoverage;

    private boolean edgePairCoverage;

    private File graphwizFile;

    public ClassAnalyzer(final ExecutionData execData, final StringPool stringPool, final boolean edgeCoverage,
                         final boolean edgePairCoverage, File graphwizFile) {
        super(Opcodes.ASM9);
        this.execData = execData;
        this.stringPool = stringPool;
        this.edgeCoverage = edgeCoverage;
        this.edgePairCoverage = edgePairCoverage;
        this.graphwizFile = graphwizFile;
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


                final BitSet nodeData;

                final MethodCoverage methodCoverage = new MethodCoverage(name, desc);

                if (edgePairCoverage) {
                    final ArrayList<Edge> edges = Edge.getEdges(flowAnalyzer.getSuccessors(), flowAnalyzer.getLeaders());
                    final ArrayList<Edge[]> edgePairs = Edge.getEdgePairs(edges);
                    nodeData = getData(execData.getData(), edgePairs.size());
                    edgePairReport(lines, flowAnalyzer, edgePairs, nodeData, methodCoverage);
                } else if (edgeCoverage) {
                    final ArrayList<Edge> edges = Edge.getEdges(flowAnalyzer.getSuccessors(), flowAnalyzer.getLeaders());
                    nodeData = getData(execData.getData(), edges.size());
                    edgeReport(lines, flowAnalyzer, edges, nodeData, methodCoverage);
                } else {
                    nodeData = getData(execData.getData(), flowAnalyzer.getBasicBlocks().length);
                    nodeReport(lines, flowAnalyzer, nodeData, methodCoverage);
                }

                if (graphwizFile != null) {
                    FileOutputStream output = null;
                    try {
                        output = new FileOutputStream(graphwizFile);
                        //GraphwizGenerator.write(flowAnalyzer, output, methodCoverage, lines, edgeCoverage, edgePairCoverage);
                    } catch (FileNotFoundException e) {
                        System.err.println("Failed: " + e.getLocalizedMessage());
                    } finally {
                        try {
                            output.close();
                        } catch (IOException e) {
                            System.err.println("Failed: " + e.getLocalizedMessage());
                        }
                    }
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

            Collection<Integer> coveredLines = getCoveredLines(lines, basicBlocks[b]);
            methodCoverage.increment(coveredNode, coveredLines);
        }

        if (methodCoverage.getCounter().getTotalCount() > 0) {
            coverage.addMethod(methodCoverage);
        }
    }

    private void edgeReport(int[] lines, final FlowAnalyzer<Value> flowAnalyzer, List<Edge> edges, final BitSet nodeData, final MethodCoverage methodCoverage) {
        final int[][] basicBlocks = flowAnalyzer.getBasicBlocks();


        for (int e = 0; e < edges.size(); e++) {
            final boolean coveredEdge = nodeData.get(e);
            Edge current = edges.get(e);
            current.covered = coveredEdge;

            Collection<Integer> coveredLines = getCoveredLines(lines, basicBlocks[current.initialNode]);
            Integer lastLineBegin = (Integer) coveredLines.toArray()[coveredLines.size() - 1];
            coveredLines = getCoveredLines(lines, basicBlocks[current.finalNode]);
            Integer firstLineEnd = (Integer) coveredLines.toArray()[0];

            methodCoverage.increment(e, coveredEdge, edges, lastLineBegin, firstLineEnd);
        }

        if (methodCoverage.getCounter().getTotalCount() > 0) {
            coverage.addMethod(methodCoverage);
        }
    }

    private void edgePairReport(int[] lines, FlowAnalyzer<Value> flowAnalyzer, ArrayList<Edge[]> edgePairs, BitSet nodeData, MethodCoverage methodCoverage) {

        final int[][] basicBlocks = flowAnalyzer.getBasicBlocks();

        for (int p = 0; p < edgePairs.size(); p++) {
            final boolean coveredEdgePair = nodeData.get(p);
            Edge[] current = edgePairs.get(p);
            current[0].covered = coveredEdgePair;
            current[1].covered = coveredEdgePair;

            Collection<Integer> coveredLines = getCoveredLines(lines, basicBlocks[current[0].initialNode]);
            Integer lastLineBeginFirstEdge = (Integer) coveredLines.toArray()[coveredLines.size() - 1];
            coveredLines = getCoveredLines(lines, basicBlocks[current[0].finalNode]);
            Integer firstLineEndFirstEdge = (Integer) coveredLines.toArray()[0];

            coveredLines = getCoveredLines(lines, basicBlocks[current[1].initialNode]);
            Integer lastLineBeginLastEdge = (Integer) coveredLines.toArray()[coveredLines.size() - 1];
            coveredLines = getCoveredLines(lines, basicBlocks[current[1].finalNode]);
            Integer firstLineEndLastEdge = (Integer) coveredLines.toArray()[0];

            methodCoverage.increment(p, coveredEdgePair, edgePairs, lastLineBeginFirstEdge,
                    firstLineEndFirstEdge, lastLineBeginLastEdge, firstLineEndLastEdge);
        }

        if (methodCoverage.getCounter().getTotalCount() > 0) {
            coverage.addMethod(methodCoverage);
        }

    }

    private Collection<Integer> getCoveredLines(int[] lines, int[] basicBlock) {
        int[] coveredInstructions = basicBlock;
        Collection<Integer> coveredLines = new TreeSet<Integer>();
        for (int i = 0; i < coveredInstructions.length; i++) {
            coveredLines.add(lines[coveredInstructions[i]]);
        }
        return coveredLines;
    }

    private int incrementWindow(final int n) {
        return window += (n + 63) / 64;
        //return window += n / 64;
    }

    public ClassCoverage getCoverage() {
        return coverage;
    }

}
