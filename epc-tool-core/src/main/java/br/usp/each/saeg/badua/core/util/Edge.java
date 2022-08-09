package br.usp.each.saeg.badua.core.util;

import java.util.ArrayList;

public class Edge {
    public int initialNode;
    public int finalNode;

    public static ArrayList<Edge> getEdges(int[][] successors, int[] leaders) {
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

}
