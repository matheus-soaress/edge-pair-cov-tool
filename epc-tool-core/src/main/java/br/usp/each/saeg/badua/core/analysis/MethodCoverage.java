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

import static org.jacoco.core.internal.analysis.CounterImpl.COUNTER_0_1;
import static org.jacoco.core.internal.analysis.CounterImpl.COUNTER_1_0;

import java.util.*;

import br.usp.each.saeg.badua.core.util.Edge;
import org.jacoco.core.analysis.ICounter;

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

    private final List<Edge> edges = new ArrayList<Edge>();

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

    public List<Edge> getEdges() {
        return edges;
    }

    public void increment(final ICounter counter) {
        this.counter = this.counter.increment(counter);
        if (this.counter.getCoveredCount() > 0) {
            methodCounter = COUNTER_0_1;
        }
    }

    public void increment(final boolean covered, Collection<Integer> lines) {
        for (Integer line: lines) {
            this.lines.add(new SourceCodeLine(line, covered));
        }
        increment(covered ? COUNTER_0_1 : COUNTER_1_0);
    }

    public void increment(final int edge, final boolean covered, final List<Edge> edges) {
        this.edges.add(edges.get(edge));
        increment(covered ? COUNTER_0_1 : COUNTER_1_0);
    }
}
