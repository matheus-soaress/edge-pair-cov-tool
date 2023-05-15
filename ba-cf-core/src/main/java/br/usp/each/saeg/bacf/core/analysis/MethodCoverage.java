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
package br.usp.each.saeg.bacf.core.analysis;

import static org.jacoco.core.internal.analysis.CounterImpl.COUNTER_0_1;
import static org.jacoco.core.internal.analysis.CounterImpl.COUNTER_1_0;

import java.util.*;

import br.usp.each.saeg.bacf.core.util.Edge;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.internal.analysis.CounterImpl;

public class MethodCoverage extends CoverageNode {

    public static final int UNKNOWN_LINE = -1;

    private final Collection<SourceLineDefUseChain> defUses = new ArrayList<SourceLineDefUseChain>();

    private final Collection<SourceCodeLine> lines = new TreeSet<SourceCodeLine>(new Comparator<SourceCodeLine>() {
        @Override
        public int compare(SourceCodeLine o1, SourceCodeLine o2) {
            if (o1.line > o2.line) {
                return 1;
            } else if (o1.line < o2.line) {
                return -1;
            } else {
                return 0;
            }
        }
    });

    private final Collection<Edge> edges = new TreeSet<Edge>();

    private final Collection<Edge[]> edgePairs = new TreeSet<Edge[]>(new Comparator<Edge[]>() {
        @Override
        public int compare(Edge[] o1, Edge[] o2) {
            if (o1[0].compareTo(o2[0]) != 0) {
                return o1[0].compareTo(o2[0]);
            } else if (o1[1].compareTo(o2[1]) != 0) {
                return o1[1].compareTo(o2[1]);
            } else {
                return 0;
            }
        }
    });

    private final String desc;

    public MethodCoverage(final String name, final String desc) {
        super(name);
        this.desc = desc;
        this.methodCounter = COUNTER_1_0;
    }

    public String getDesc() {
        return desc;
    }

    public Collection<SourceLineDefUseChain> getDefUses() {
        return Collections.unmodifiableCollection(defUses);
    }

    public Collection<SourceCodeLine> getLines() {
        return lines;
    }

    public Collection<Edge> getEdges() {
        return edges;
    }

    public Collection<Edge[]> getEdgePairs() {
        return edgePairs;
    }

    public CounterImpl getLineCounter() {
        return lineCounter;
    }

    public void increment(final ICounter counter) {
        this.counter = this.counter.increment(counter);
        if (this.counter.getCoveredCount() > 0) {
            methodCounter = COUNTER_0_1;
        }
    }

    public void increment(final boolean covered, Collection<Integer> lines) {
        for (Integer line: lines) {
            if(this.lines.add(new SourceCodeLine(line, covered))) {
                this.lineCounter = this.lineCounter.increment(covered ? COUNTER_0_1 : COUNTER_1_0);
            }
        }
        increment(covered ? COUNTER_0_1 : COUNTER_1_0);
    }

    public void increment(final int edge, final boolean covered, final List<Edge> edges, final Integer lastLineBegin, final Integer firstLineEnd) {
        Edge e = edges.get(edge);
        e.lastLineInitialNode = lastLineBegin;
        e.firstLineFinalNode = firstLineEnd;
        this.edges.add(e);
        increment(covered ? COUNTER_0_1 : COUNTER_1_0);
    }

    public void increment(final int edgePair, final boolean covered, final List<Edge[]> edgePairs,
                          Integer lastLineBeginFirstEdge, Integer firstLineEndFirstEdge,
                          Integer lastLineBeginLastEdge, Integer firstLineEndLastEdge) {
        Edge[] ep = edgePairs.get(edgePair);
        ep[0].lastLineInitialNode = lastLineBeginFirstEdge;
        ep[0].firstLineFinalNode = firstLineEndFirstEdge;
        ep[1].lastLineInitialNode = lastLineBeginLastEdge;
        ep[1].firstLineFinalNode = firstLineEndLastEdge;
        this.edgePairs.add(ep);
        increment(covered ? COUNTER_0_1 : COUNTER_1_0);
    }
}
