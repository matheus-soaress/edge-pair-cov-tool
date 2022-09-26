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
package br.usp.each.saeg.badua.core.util;

import java.util.ArrayList;

public class Edge implements Comparable {

    public int initialNode;
    public int finalNode;

    public int lastLineInitialNode;
    public int firstLineFinalNode;

    public boolean covered;

    public Edge () {

    }

    public Edge (final int initialNode, final int finalNode) {
        this.initialNode = initialNode;
        this.finalNode = finalNode;
    }

    public Edge (final Edge edge) {
        this.initialNode = edge.initialNode;
        this.finalNode = edge.finalNode;
        this.covered = edge.covered;
        this.lastLineInitialNode = edge.lastLineInitialNode;
        this.firstLineFinalNode = edge.firstLineFinalNode;
    }

    public static ArrayList<Edge> getEdges(final int[][] successors, final int[] leaders) {
        ArrayList<Edge> edges = new ArrayList<Edge>();
        Edge edge;

        for (int i = 0; i < successors.length; i++) {

            for (int suc : successors[i]) {
                if (leaders[i] != leaders[suc]) {

                    // Se o nó da instrução sucessora for diferente, então
                    // deve haver uma aresta entre o nó atual e o nó da
                    // instrução sucessora
                    edge = new Edge();
                    edge.initialNode = leaders[i];
                    edge.finalNode = leaders[suc];
                    edges.add(edge);
                }
            }
        }
        return edges;
    }

    public static ArrayList<Edge[]> getEdgePairs(final ArrayList<Edge> edges) {

        ArrayList<Edge[]> edgePairs = new ArrayList<Edge[]>();


        for (Edge edge : edges) {
            for (Edge otherEdge : edges) {
                if (edge.finalNode == otherEdge.initialNode) {
                    edgePairs.add(new Edge[] {new Edge(edge), new Edge (otherEdge)});
                }
            }
        }

        return edgePairs;
    }

    @Override
    public int compareTo(Object o) {
        Edge other = (Edge) o;
        if (this.initialNode > other.initialNode) {
            return 1;
        } else if (this.initialNode < other.initialNode) {
            return -1;
        } else if (this.finalNode > other.finalNode) {
            return 1;
        } else if (this.finalNode < other.finalNode) {
            return -1;
        } else {
            return 0;
        }
    }
}