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
package br.usp.each.saeg.badua.cli;

import static br.usp.each.saeg.badua.core.analysis.SourceLineDefUseChain.NONE;
import static org.jacoco.report.internal.xml.XMLCoverageWriter.createChild;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import br.usp.each.saeg.badua.core.analysis.*;
import br.usp.each.saeg.badua.core.util.Edge;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.report.internal.xml.XMLDocument;
import org.jacoco.report.internal.xml.XMLElement;

public class XMLCoverageWriter {

    private XMLCoverageWriter() {
        // No instances
    }

    public static void write(final List<ClassCoverage> classes, final FileOutputStream output, final boolean edges)
            throws IOException {
        final CoverageNode sum = new CoverageNode("");
        final XMLElement root = new XMLDocument("report", null, null, "UTF-8", true, output);
        for (final ClassCoverage c : classes) {
            writeClass(c, root, edges);
            sum.increment(c);
        }
        writeCounters(sum, root, edges);
        root.close();
    }

    private static void writeClass(final ClassCoverage c, final XMLElement parent, final boolean edges) throws IOException {
        final XMLElement element = createChild(parent, "class", c.getName());
        for (final MethodCoverage m : c.getMethods()) {
            writeMethod(m, element, edges);
        }
        writeCounters(c, element, edges);
    }

    private static void writeMethod(final MethodCoverage m, final XMLElement parent, final boolean edges) throws IOException {
        final XMLElement element = createChild(parent, "method", m.getName());
        element.attr("desc", m.getDesc());

        if (edges) {
            for (final Edge edge : m.getEdges()) {
                writeEdge(edge, element);
            }
        } else {
            for (final SourceCodeLine line : m.getLines()) {
                writeLine(line, element);
            }
        }
        writeCounters(m, element, edges);
    }

    private static void writeEdge(Edge edge, XMLElement parent) throws IOException {
        final XMLElement element = parent.element("edge");
        element.attr("beg", edge.initialNode);
        element.attr("end", edge.finalNode);
        element.attr("covered", edge.covered ? 1 : 0);
    }

    private static void writeDU(final SourceLineDefUseChain du, final XMLElement parent) throws IOException {
        final XMLElement element = parent.element("du");
        element.attr("var", du.var);
        element.attr("def", du.def);
        element.attr("use", du.use);
        if (du.target != NONE) {
            element.attr("target", du.target);
        }
        element.attr("covered", du.covered ? 1 : 0);
    }

    private static void writeCounters(final CoverageNode node, final XMLElement parent, final boolean edges) throws IOException {
        if (edges) {
            writeCounter(node.getCounter(), "EDGE", parent);
        } else {
            writeCounter(node.getCounter(), "NODE", parent);
        }
        writeCounter(node.getMethodCounter(), "METHOD", parent);
        writeCounter(node.getClassCounter(), "CLASS", parent);
    }

    private static void writeCounter(final ICounter counter, final String type, final XMLElement parent)
            throws IOException {
        if (counter.getTotalCount() > 0) {
            final XMLElement element = parent.element("counter");
            element.attr("type", type);
            element.attr("missed", counter.getMissedCount());
            element.attr("covered", counter.getCoveredCount());
        }
    }

    private static void writeLine(final SourceCodeLine line, final XMLElement parent) throws IOException {
        final XMLElement element = parent.element("line");
        element.attr("number", line.line);
        element.attr("covered", line.covered ? 1 : 0);
    }

}
