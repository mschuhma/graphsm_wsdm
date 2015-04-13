/*
 * Copyright (c) 2012, Søren Atmakuri Davidsen
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package dk.aaue.sna.generate;

import dk.aaue.sna.util.JGraphTUtil;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.VertexFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Methods for adding random errors to a graph. See [1]
 *
 * [1] Stephen Borgatti.
 *
 * @author Soren <soren@tanesha.net>
 */
public class BorgattiRandomErrorBuilder<V, E> implements GraphErrorBuilder<V, E> {

    private Random rand;
    private VertexFactory<V> vVertexFactory;
    private boolean keepComplete;

    /**
     * Construct with a given random object.
     *
     * @param seed a random seed
     * @param vVertexFactory a vertex factory
     * @param keepComplete Should we keep the graph complete?
     */
    public BorgattiRandomErrorBuilder(VertexFactory<V> vVertexFactory, Long seed, boolean keepComplete) {
        this.vVertexFactory = vVertexFactory;
        this.rand = new Random();
        if (seed != null) {
            rand.setSeed(seed);
        }
        this.keepComplete = keepComplete;
    }

    public BorgattiRandomErrorBuilder(VertexFactory<V> vVertexFactory, Long seed) {
        this(vVertexFactory, seed, false);
    }

    public BorgattiRandomErrorBuilder(VertexFactory<V> vVertexFactory) {
        this(vVertexFactory, null, false);
    }

    /**
     * Add nodes to graph.
     * <p/>
     * New nodes are added, for each new node, a random node from the graph is selected, and the degree
     * of the random node is used as degree of the new node. The degree is created by selecting
     * n random nodes and creating edges to the new node.
     *
     * @param graph  The graph to add errors to.
     * @param errors How many new nodes to add, eg. 1.0 doubles the number of nodes in the graph.
     */
    public void errorsNodeAdd(Graph<V, E> graph, double errors) {

        if (errors < 0.0 || errors > 1.0)
            throw new RuntimeException("Errors outside [0, 1]");

        int newNodes = (int) Math.ceil(graph.vertexSet().size() * errors);

        // add the new nodes
        for (int i = 0; i < newNodes; i++) {
            // nodes before adding a new
            List<V> nodes = new ArrayList<V>();
            nodes.addAll(graph.vertexSet());

            V newErrorNode = vVertexFactory.createVertex();
            graph.addVertex(newErrorNode);

            Collections.shuffle(nodes, rand);

            // add edges (pick random node, add that amount of edges)
            V randomNode = nodes.get(rand.nextInt(nodes.size()));
            int randomNodeDegree = graph.edgesOf(randomNode).size();

            for (int j = 0; j < randomNodeDegree; j++) {

                // pick a random node from the shuffled list and link it up
                randomNode = nodes.get(j);

                graph.addEdge(newErrorNode, randomNode);
            }
        }
    }

    /**
     * Adds random edges to a graph.
     * <p/>
     * For each new edge, a random node which has room for another edge is found.
     * For this node, a random node in the possible set of neighbour nodes is selected, and
     * an edge between the two nodes is created.
     *
     * @param graph  The graph to add edges to
     * @param errors The percentage of error edges to add in relation to existing number of edges.
     *               Eg. 1.0 doubles the number of edges (if possible)
     */
    public void errorsEdgeAdd(Graph<V, E> graph, double errors) {

        if (errors < 0.0 || errors > 1.0)
            throw new RuntimeException("Errors outside [0, 1]");

        List<V> nodes = new ArrayList<V>(graph.vertexSet());

        int numEdges = graph.edgeSet().size();

        int newEdges = (int) Math.ceil(numEdges * errors);

        int maxEdges = JGraphTUtil.maxEdges(graph);

        // we can't make more than max edges in the graph
        newEdges = Math.min(newEdges, maxEdges - numEdges);

        // System.out.println("max edges=" + maxEdges + ", error=" + errors + ", new edges=" + newEdges);

        // we can't add any errors to this graph
        // if (newEdges == 0)
        //	throw new RuntimeException("No errors can be added; graph has " + numEdges + "/" + maxEdges + " already.");

        int maxDegree = nodes.size() - 1;

        // add the new nodes
        for (int i = 0; i < newEdges; i++) {

            // shuffle nodes, so we get a random set of possible edges
            Collections.shuffle(nodes, rand);

            // find a random src
            V src = null;
            for (V node : nodes) {

                int degree = graph.edgesOf(node).size();

                // lucky, we found a good node
                if (degree < maxDegree) {
                    src = node;
                    break;
                }
            }

            if (src == null)
                throw new RuntimeException("Graph is complete :-/");

            // find possible destinations; not the node itself and not the
            // neighbours it already has.
            List<V> dstNodes = new ArrayList<V>(nodes);
            dstNodes.removeAll(Graphs.neighborListOf(graph, src));
            dstNodes.remove(src);

            // System.out.println("Possible new neighbors to " + src + ": " + dstNodes);

            // add edges with src and a random pick from the dst nodes
            graph.addEdge(src, dstNodes.get(rand.nextInt(dstNodes.size())));
        }
    }

    /**
     * Removes a portion of edges from the graph.
     * <p/>
     * For each edge to remove, a random edge is selected from the set of edges, which is then removed
     * from the graph.
     *
     * @param graph  The graph to remove edges from
     * @param errors Proportion of edges to remove. Eg. 1.0 removes all edges, 0.5 removes half
     *               of the edges.
     */
    public void errorsEdgeRemove(Graph<V, E> graph, double errors) {

        if (errors < 0.0 || errors > 1.0)
            throw new RuntimeException("Errors outside [0, 1]");

        List<E> edges = new ArrayList<E>(graph.edgeSet());

        // randomize the list of edges.
        Collections.shuffle(edges, rand);

        int numEdges = edges.size();
        int delEdges = (int) Math.ceil(numEdges * errors);

        // should not be nessecary :/
        delEdges = Math.min(numEdges, delEdges);

        // remove edges
        /*
        int removed = 0;
        for (E e : edges) {

            if (keepComplete) {
                V src = graph.getEdgeSource(e);
                V dst = graph.getEdgeTarget(e);

                int srcDeg = graph.edgesOf(src).size();
                int dstDeg = graph.edgesOf(dst).size();

                if (srcDeg > 1 && dstDeg > 1) {
                    graph.removeEdge(e);
                    removed++;
                }
            }
            else {
                graph.removeEdge(e);
                removed++;
            }

            if (removed == delEdges)
                break;
        }

        if (removed != delEdges) {
            throw new RuntimeException("Error removing edges, graph becomes incomplete and we required complete graph");
        }
        */

        for (int i = 0; i < delEdges; i++) {
            E edge = edges.get(i);

            graph.removeEdge(edge);
        }
    }

    /**
     * Removes nodes from a graph.
     * <p/>
     * A list of possible nodes to remove is created, by removing the "holy" nodes from the list of all nodes.
     * The list is shuffled for randomness in node removal. The proportion of nodes to remove, is removed
     * one by one.
     *
     * @param graph     The graph to remove nodes from
     * @param errors    The proportion of nodes to remove. Eg. 1.0 removes all nodes, 0.5 removes half the nodes.
     * @param holyNodes A list of nodes not to touch while removing.
     */
    public void errorsNodeRemove(Graph<V, E> graph, double errors, List<V> holyNodes) {

        if (errors < 0.0 || errors > 1.0)
            throw new RuntimeException("Errors outside [0, 1]");

        // create list of nodes, and remove the holy nodes from the list.
        List<V> nodes = new ArrayList<V>(graph.vertexSet());
        nodes.removeAll(holyNodes);

        // randomize the list of nodes.
        Collections.shuffle(nodes, rand);

        // find how many nodes to remove
        int numNodes = nodes.size();
        int delNodes = (int) Math.ceil(numNodes * errors);

        // if we don't look at the "holy" nodes, then there might not be enough to remove.
        delNodes = Math.min(delNodes, nodes.size());

		// remove nodes
		for (int i = 0; i < delNodes; i++) {
			V node = nodes.get(i);
			graph.removeVertex(node);
		}
	}
}
