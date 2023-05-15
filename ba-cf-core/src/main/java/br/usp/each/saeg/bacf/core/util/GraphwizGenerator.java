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
package br.usp.each.saeg.bacf.core.util;

/*import br.usp.each.saeg.asm.defuse.FlowAnalyzer;
import br.usp.each.saeg.asm.defuse.Value;
import br.usp.each.saeg.bacf.core.analysis.ClassCoverage;
import br.usp.each.saeg.bacf.core.analysis.MethodCoverage;
import jdk.internal.org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LineNumberNode;

import java.io.FileOutputStream;
import java.util.*;*/

public class GraphwizGenerator {

    /*String name;
    Program p;
    int[] lines;

    public static void write(final FlowAnalyzer<Value> flowAnalyzer, final FileOutputStream output,
                             final MethodCoverage coverage, final int[] lines, final boolean edges,
                             final boolean edgePairs) {
        String graph = graphDefUseToDot(flowAnalyzer, coverage, lines);
    }

    public static String graphDefUseToDot(final FlowAnalyzer<Value> flowAnalyzer, final MethodCoverage coverage,
                                          final int[] lines) {
        final StringBuilder sb = new StringBuilder();
        final boolean printLines = true;

        sb.append("digraph " + coverage.getDesc() + " {\n");

        int[][] basicBlocks = flowAnalyzer.getBasicBlocks();

        int i = 0;
        int[] blk;
        Integer[] curBlockLines;
        while (i < basicBlocks.length) {
            blk = basicBlocks[i];

            Object[] instr = p.getGraph().get(blk.id()).lines().toArray();

            curBlockLines = getLines(lines, basicBlocks[i]);
            int firstLine = curBlockLines[0];
            int lastLine = curBlockLines[curBlockLines.length - 1];

            if (printLines) {
                sb.append(i + " [label=\"" + i + "\\n" + firstLine + "-" + lastLine + "\"]");
            } else {
                sb.append(i + " [label=\"" + i + "\"]");
            }
            sb.append(i + " [label=\"" + i + "\"]");

            sb.append("\n");
        }

        sb.append("{\n" +
                "node [shape=plaintext, fontsize=14];\n");
        it = p.getGraph().iterator();
        while (it.hasNext()) {
            Block blk = it.next();
            if (blk.defs().size() == 0 && blk.cuses().size() == 0)
                continue;
            sb.append("setsNode_" + blk.id());
            sb.append(" [label=\"");
            if (blk.defs().size() != 0)
                sb.append(printDefSet(blk));
            if (blk.cuses().size() != 0) {
                if (blk.defs().size() != 0)
                    sb.append("\\n");
                sb.append(printCuseSet(blk));
            }
            sb.append("\"];\n");
        }
        sb.append("}\n");

        it = p.getGraph().iterator();
        while (it.hasNext()) {
            Block blk = it.next();
            if (blk.defs().size() == 0 && blk.cuses().size() == 0)
                continue;
            sb.append("{rank = same; ");
            sb.append(blk.id() + " ; " + " setsNode_" + blk.id());
            sb.append("}\n");
        }

        it = p.getGraph().iterator();
        while (it.hasNext()) {
            Block blk = it.next();
            if (!p.getGraph().neighbors(blk.id()).isEmpty()) {
                for (Block suc : p.getGraph().neighbors(blk.id())) {
                    sb.append(blk.id() + " -> ");
                    sb.append(suc.id());
                    if (blk.puses().size() != 0) {
                        sb.append("[label=\"");
                        sb.append(printPuseSet(blk, suc));
                        sb.append("\",fontsize=14]");
                    }

                    sb.append(";");
                }
                sb.append("\n");
            }
        }

        sb.append("}\n");
        return sb.toString();
    }

    private static Integer[] getLines(int[] lines, int[] basicBlock) {
        int[] coveredInstructions = basicBlock;
        Collection<Integer> coveredLines = new ArrayList<Integer>();
        for (int i = 0; i < coveredInstructions.length; i++) {
            coveredLines.add(lines[coveredInstructions[i]]);
        }
        return (Integer[]) coveredLines.toArray();
    }*/

}
