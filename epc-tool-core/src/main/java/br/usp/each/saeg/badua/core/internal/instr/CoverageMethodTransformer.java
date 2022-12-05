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
package br.usp.each.saeg.badua.core.internal.instr;

import br.usp.each.saeg.asm.defuse.*;
import br.usp.each.saeg.badua.core.util.Edge;
import br.usp.each.saeg.commons.BitSetUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import java.util.ArrayList;
import java.util.BitSet;

public class CoverageMethodTransformer extends MethodTransformer {

    private final String className;

    private final IdGenerator nodeIdGen;

    private final boolean edgeCoverage;

    private final boolean edgePairCoverage;

    public CoverageMethodTransformer(final String className, final IdGenerator nodeIdGen, final boolean edges, final boolean edgePairs) {
        this.className = className;
        this.nodeIdGen = nodeIdGen;
        this.edgeCoverage = edges;
        this.edgePairCoverage = edgePairs;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void transform(final MethodNode methodNode) {

        final DefUseInterpreter interpreter = new DefUseInterpreter();
        final FlowAnalyzer<Value> flowAnalyzer = new FlowAnalyzer<Value>(interpreter);
        final DefUseAnalyzer analyzer = new DefUseAnalyzer(flowAnalyzer, interpreter);
        try {
            analyzer.analyze(className, methodNode);
        } catch (final AnalyzerException e) {
            throw new RuntimeException(e);
        }

        if (edgePairCoverage) {
            edgePairInstrument(flowAnalyzer.getSuccessors(), flowAnalyzer.getBasicBlocks(), flowAnalyzer.getLeaders(), methodNode);
        } else if (edgeCoverage) {
            edgeInstrument(flowAnalyzer.getSuccessors(), flowAnalyzer.getBasicBlocks(), flowAnalyzer.getLeaders(), methodNode);
        } else {
            nodeInstrument(flowAnalyzer.getBasicBlocks(), methodNode);
        }

    }

    private void nodeInstrument(int[][] basicBlocks, MethodNode methodNode) {
        // inicia um conjunto do bloco que sera usado para "setar" o bit do no coberto
        final BitSet[] noAtualCoberto = new BitSet[basicBlocks.length];
        for (int b = 0; b < basicBlocks.length; b++) {
            noAtualCoberto[b] = new BitSet(basicBlocks.length);
            noAtualCoberto[b].set(b);
        }

        // first/last valid instructions
        final AbstractInsnNode[] first = getFirst(basicBlocks, methodNode);

        AbstractInsnNode insn = methodNode.instructions.getFirst();

        final int nodeWindows = (basicBlocks.length + 63) / 64;
        final int[] nodeIndexes = new int[nodeWindows];
        // insere uma ponta de prova inicial para fluxo de controle (imitando o fluxo de dados)
        for (int w = 0; w < nodeWindows; w++) {
            nodeIndexes[w] = nodeIdGen.nextId();
            LabelFrameNode.insertBefore(insn, methodNode.instructions, init(basicBlocks.length, methodNode, w));
        }

        // insere pontas de prova comuns
        for (int b = 0; b < basicBlocks.length; b++) {
            final long[] lNoAtualCoberto = BitSetUtils.toLongArray(noAtualCoberto[b], nodeWindows);

            for (int w = 0; w < nodeWindows; w++) {
                final Probe p = probe(basicBlocks.length, methodNode, w);

                p.currentActiveElement = lNoAtualCoberto[w];

                LabelFrameNode.insertBefore(first[b], methodNode.instructions, p);
            }
        }

        while (insn != null) {
            if (insn instanceof FrameNode) {
                final FrameNode frame = getFrame(methodNode, (FrameNode) insn);

                final Integer controlFlowType = typeOfVars(basicBlocks.length);
                for (int i = 0; i < nodeWindows; i++) {
                    frame.local.add(controlFlowType);
                }
            } else if (isReturn(insn.getOpcode())) {
                // insere ponta de prova final
                for (int w = 0; w < nodeWindows; w++) {
                    final Probe p = update(basicBlocks.length, methodNode, w, nodeIndexes[w]);
                    LabelFrameNode.insertBefore(insn, methodNode.instructions, p);
                }
            }
            insn = insn.getNext();
        }

        methodNode.maxLocals = methodNode.maxLocals + nodeWindows * numOfBlocks(basicBlocks.length);
        methodNode.maxStack = methodNode.maxStack + 6;

    }

    private void edgeInstrument(int[][] successors, int[][] basicBlocks, int[] leaders, MethodNode methodNode) {
        final ArrayList<Edge> edges = Edge.getEdges(successors, leaders);
        final BitSet[] arestaAtualAtiva = new BitSet[basicBlocks.length];

        for (int b = 0; b < basicBlocks.length; b++) {
            // inicia um conjunto do bloco que sera usado para "setar" o bit da aresta ativa
            arestaAtualAtiva[b] = new BitSet(edges.size());
            for (int e = 0; e < edges.size(); e++) {
                if (edges.get(e).initialNode == b || edges.get(e).finalNode == b) {
                    arestaAtualAtiva[b].set(e);
                }
            }
        }
        final AbstractInsnNode[] first = getFirst(basicBlocks, methodNode);

        AbstractInsnNode insn = methodNode.instructions.getFirst();

        final int edgeWindows = (edges.size() + 63) / 64;
        final int[] edgeIndexes = new int[edgeWindows];
        for (int w = 0; w < edgeWindows; w++) {
            edgeIndexes[w] = nodeIdGen.nextId();
            LabelFrameNode.insertBefore(insn, methodNode.instructions, init(edges.size(), methodNode, w));
        }

        // insere pontas de prova comuns
        for (int b = 0; b < basicBlocks.length; b++) {

            final long[] lArestaAtualAtiva;
            if (arestaAtualAtiva[b] == null) {
                lArestaAtualAtiva = null;
            } else {
                lArestaAtualAtiva = BitSetUtils.toLongArray(arestaAtualAtiva[b], edgeWindows);
            }

            for (int w = 0; w < edgeWindows; w++) {
                final Probe p = probe(edges.size(), methodNode, w);

                if (lArestaAtualAtiva != null) {
                    p.currentActiveElement = lArestaAtualAtiva[w];
                } else {
                    p.currentActiveElement = 0L;
                }

                LabelFrameNode.insertBefore(first[b], methodNode.instructions, p);
            }
        }

        while (insn != null) {
            if (insn instanceof FrameNode) {
                final FrameNode frame = getFrame(methodNode, (FrameNode) insn);

                // begin matheus
                final Integer controlFlowType = typeOfVars(edges.size());
                for (int i = 0; i < edgeWindows; i++) {
                    frame.local.add(controlFlowType);
                    frame.local.add(controlFlowType);
                }
                // end matheus
            } else if (isReturn(insn.getOpcode())) {
                // begin matheus
                // insere ponta de prova final
                for (int w = 0; w < edgeWindows; w++) {
                    final Probe p = update(edges.size(), methodNode, w, edgeIndexes[w]);
                    LabelFrameNode.insertBefore(insn, methodNode.instructions, p);
                }
                // end matheus
            }
            insn = insn.getNext();
        }

        methodNode.maxLocals = methodNode.maxLocals + edgeWindows * numOfBlocks(edges.size());
        methodNode.maxStack = methodNode.maxStack + 6;

    }

    private void edgePairInstrument(int[][] successors, int[][] basicBlocks, int[] leaders, MethodNode methodNode) {

        final ArrayList<Edge> edges = Edge.getEdges(successors, leaders);
        final ArrayList<Edge[]> edgePairs = Edge.getEdgePairs(edges);

        final BitSet[] parDeArestasAtualAtivo = new BitSet[basicBlocks.length];

        for (int b = 0; b < basicBlocks.length; b++) {
            // inicia um conjunto do bloco que sera usado para "setar" o bit do par de arestas ativo
            parDeArestasAtualAtivo[b] = new BitSet(edgePairs.size());
            for (int e = 0; e < edgePairs.size(); e++) {
                if (edgePairs.get(e)[0].initialNode == b || edgePairs.get(e)[0].finalNode == b || edgePairs.get(e)[1].finalNode == b) {
                    parDeArestasAtualAtivo[b].set(e);
                }
            }
        }

        // first/last valid instructions
        final AbstractInsnNode[] first = getFirst(basicBlocks, methodNode);

        AbstractInsnNode insn = methodNode.instructions.getFirst();

        final int edgePairWindows = (edgePairs.size() + 63) / 64;
        final int[] edgePairIndexes = new int[edgePairWindows];
        for (int w = 0; w < edgePairWindows; w++) {
            edgePairIndexes[w] = nodeIdGen.nextId();
            LabelFrameNode.insertBefore(insn, methodNode.instructions, init(edgePairs.size(), methodNode, w));
        }

        // insere pontas de prova comuns
        for (int b = 0; b < basicBlocks.length; b++) {

            final long[] lParDeArestasAtualAtivo;
            if (parDeArestasAtualAtivo[b] == null) {
                lParDeArestasAtualAtivo = null;
            } else {
                lParDeArestasAtualAtivo = BitSetUtils.toLongArray(parDeArestasAtualAtivo[b], edgePairWindows);
            }

            for (int w = 0; w < edgePairWindows; w++) {
                final Probe p = probe(edgePairs.size(), methodNode, w);

                if (lParDeArestasAtualAtivo != null) {
                    p.currentActiveElement = lParDeArestasAtualAtivo[w];
                } else {
                    p.currentActiveElement = 0L;
                }

                LabelFrameNode.insertBefore(first[b], methodNode.instructions, p);
            }
        }

        while (insn != null) {
            if (insn instanceof FrameNode) {
                final FrameNode frame = getFrame(methodNode, (FrameNode) insn);

                final Integer controlFlowType = typeOfVars(edgePairs.size());
                for (int i = 0; i < edgePairWindows; i++) {
                    frame.local.add(controlFlowType);
                    frame.local.add(controlFlowType);
                    frame.local.add(controlFlowType);
                }
            } else if (isReturn(insn.getOpcode())) {
                // insere ponta de prova final
                for (int w = 0; w < edgePairWindows; w++) {
                    final Probe p = update(edgePairs.size(), methodNode, w, edgePairIndexes[w]);
                    LabelFrameNode.insertBefore(insn, methodNode.instructions, p);
                }
            }
            insn = insn.getNext();
        }

        methodNode.maxLocals = methodNode.maxLocals + edgePairWindows * numOfBlocks(edgePairs.size());
        methodNode.maxStack = methodNode.maxStack + 6;

    }

    private Probe init(final int size, final MethodNode methodNode, final int window) {
        if (size <= 32) {
            return new IntegerInitProbe(methodNode, edgeCoverage, edgePairCoverage);
        } else {
            return new LongInitProbe(methodNode, window, edgeCoverage, edgePairCoverage);
        }
    }

    private Probe probe(final int size, final MethodNode methodNode, final int window) {
        if (size <= 32) {
            return new IntegerProbe(methodNode, edgeCoverage, edgePairCoverage);
        } else {
            return new LongProbe(methodNode, window, edgeCoverage, edgePairCoverage);
        }
    }

    private Probe update(final int size, final MethodNode methodNode, final int window, final int index) {
        if (size <= 32) {
            return new IntegerUpdateProbe(methodNode, className, index, edgeCoverage, edgePairCoverage);
        } else {
            return new LongUpdateProbe(methodNode, window, className, index, edgeCoverage, edgePairCoverage);
        }
    }

    private boolean isReturn(final int opcode) {
        if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN)
            return true;

        return opcode == Opcodes.ATHROW;
    }

    private int numOfBlocks(int size) {
        int numVarProbe = 1;
        if (edgePairCoverage) {
            numVarProbe += 2;
        } else if (edgeCoverage) {
            numVarProbe++;
        }
        if (size <= 32) {
            // two integers
            return 1 * numVarProbe;
        } else {
            // two longs
            return 2 * numVarProbe;
        }
    }

    private Integer typeOfVars(final int nRequisitosTeste) {
        if (nRequisitosTeste <= 32) {
            return Opcodes.INTEGER;
        } else {
            return Opcodes.LONG;
        }
    }

    private AbstractInsnNode[] getFirst(int[][] basicBlocks, MethodNode methodNode) {
        // first/last valid instructions
        final AbstractInsnNode[] first = new AbstractInsnNode[basicBlocks.length];
        final AbstractInsnNode[] last = new AbstractInsnNode[basicBlocks.length];
        for (int b = 0; b < basicBlocks.length; b++) {
            for (final int insnIndex : basicBlocks[b]) {
                final AbstractInsnNode insn = methodNode.instructions.get(insnIndex);

                // skip
                switch (insn.getType()) {
                    case AbstractInsnNode.LABEL:
                    case AbstractInsnNode.FRAME:
                    case AbstractInsnNode.LINE:
                        continue;
                }

                if (first[b] == null) {
                    first[b] = insn;
                }
                last[b] = insn;
            }
        }
        return first;
    }

    private FrameNode getFrame(MethodNode methodNode, FrameNode insn) {
        final FrameNode frame = insn;
        frame.local = new ArrayList<Object>(frame.local);
        int size = 0;
        for (final Object obj : frame.local) {
            size++;
            if (obj.equals(Opcodes.DOUBLE) || obj.equals(Opcodes.LONG)) {
                size++;
            }
        }
        while (size < methodNode.maxLocals) {
            frame.local.add(Opcodes.TOP);
            size++;
        }
        return frame;
    }

}