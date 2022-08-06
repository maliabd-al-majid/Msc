package Utlity;

import GraphLibs.Edge;
import GraphLibs.Graph;
import GraphLibs.Node;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author Mohamed Nadeem
 */
public class GraphUtility {
    public static Graph getCopy(Graph input, OWLOntology ontology) {
        Graph graph = new Graph(ontology);
        for (int i=0;i<input.getNodes().size();i++
        ) {

            graph.addNode(input.getNode(i).concept(), input.getNode(i).individual());
            for (Edge e : input.getNodeEdges(input.getNode(i).individual())) {
                graph.addNode(input.getNode(e.to()).concept(), input.getNode(e.to()).individual());
                graph.addEdge(input.getNode(e.from()).individual(), input.getNode(e.to()).individual(), e.property());
            }
        }

        return graph;

    }
    public static void print(Graph graph) {
        System.out.println("- Canonical Graph:");
        for (int i=0;i<graph.getNumberOfNodes();i++) {
            System.out.println(graph.getNode(i)+": Concepts("+graph.getNode(i).concept()+")"+", Individual:"+graph.getNode(i).individual());
            System.out.println("Edges: " + graph.getNodeEdges(graph.getNode(i).individual()));
        }
    }
    public static boolean isCyclic(Graph graph) {

        // Mark all the vertices as not visited and
        // not part of recursion stack
        boolean[] visited = new boolean[graph.getNumberOfNodes()];
        boolean[] recStack = new boolean[graph.getNumberOfNodes()];


        // Call the recursive helper function to
        // detect cycle in different DFS trees
        for (int i = 0; i < graph.getNumberOfNodes(); i++)
            if (isCyclicUtil(graph,i, visited, recStack))
                return true;

        return false;
    }
    private static boolean isCyclicUtil(Graph graph,int i, boolean[] visited, boolean[] recStack) {

        // Mark the current node as visited and
        // part of recursion stack
        if (recStack[i])
            return true;

        if (visited[i])
            return false;

        visited[i] = true;

        recStack[i] = true;
        List<Integer> children = new ArrayList<>();
        for(Node n:graph.getSuccessors(graph.getNode(i)))
            children.add(n.label());
        for (Integer c: children)
            if (isCyclicUtil(graph,c, visited, recStack))
                return true;

        recStack[i] = false;

        return false;
    }
}
