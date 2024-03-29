package MscConstruction;

import CanonicalConstruction.CanonicalModelFactory;
import GraphLibs.Edge;
import GraphLibs.Graph;
import GraphLibs.Node;
import SimmulationConstruction.SimulationChecker;

import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.awt.*;
import java.awt.desktop.SystemSleepEvent;
import java.util.*;
import java.util.List;

import static Utlity.GraphUtility.*;

/**
 * @author Mohamed Nadeem
 */
public class MscBuilderFactory {
    private final Graph canonicalModel;
    private Graph graphConstructed;
    private final OWLOntology ontology;
    private final OWLNamedIndividual owlIndividual;
    private final CanonicalModelFactory canonicalModelFactory;
    private final LinkedList<Node> visited;
    private final LinkedList<Node> nonVisited;
    private final SimulationChecker simulationChecker;

    public Graph getGraphConstructed() {
        return graphConstructed;
    }

    public Graph getCanonicalModel() {
        return canonicalModel;
    }

    public Graph getCanonicalGraphConstructed() {
        Graph temp = getCopy(graphConstructed, ontology);
        canonicalModelFactory.canonicalFromGraph(temp);
        return temp;
    }

    public MscBuilderFactory(OWLOntology ontology, OWLNamedIndividual owlIndividual) throws OWLOntologyCreationException {
        this.ontology = ontology;

        this.owlIndividual = owlIndividual;
        this.canonicalModel = new Graph(ontology);
        this.graphConstructed = new Graph(ontology);
        visited = new LinkedList<>();
        nonVisited = new LinkedList<>();
        canonicalModelFactory = new CanonicalModelFactory(ontology);

        simulationChecker = new SimulationChecker();
        //visitNode(graphConstructed.getRoot());

    }

    public boolean buildMsc() {

        canonicalModelFactory.canonicalFromIndividual(owlIndividual, canonicalModel);
        System.out.println("-----------------------------------------");
        System.out.println("- Building MSC Graph");
        System.out.println("-----------------------------------------");
        if (canonicalModel.getRoot() != null) {
            nonVisited.add(canonicalModel.getRoot());
            graphConstructed.addNode(canonicalModel.getRoot().concept(), canonicalModel.getRoot().individual());
            visitNode(nonVisited.poll());
        } else {
            System.out.println(" Canonical Model of Individual is Empty");
            return false;
        }

        //System.out.println("-----------------------------------------");
        System.out.println("- Constructing the Minimal Msc Graph");
        //graphConstructed.DFS(graphConstructed.getRoot());
        Graph t= getCopy(graphConstructed,ontology);
        findMinimalGraph(t);
        Graph finalGraph= getCopy(t,ontology);
        System.out.println("-----------------------------------------");
        //checking whether Msc Exists or not.
        Graph temp = getCopy(graphConstructed, ontology);
        canonicalModelFactory.canonicalFromGraph(temp);
        graphConstructed= getCopy(finalGraph,ontology);
        return simulationChecker.checkSimulation(canonicalModel, temp) && simulationChecker.checkSimulation(temp, canonicalModel) && !isCyclic(finalGraph);

    }

    private void visitNode(Node v) {
      //  System.out.println(v);
        if (!visited.contains(v)) {
            Graph temp = getCopy(graphConstructed, ontology);
            visited.add(v);
            canonicalModelFactory.canonicalFromGraph(temp);
            for (Edge e : canonicalModel.getNodeEdgesExcludingFresh(v.individual())) {
                //   if (!simulationChecker.checkSimulation(canonicalModel, temp)) {
                //Need to check whether edge already exists
                boolean edgeIsSimulated = false;
                for (Edge tempEdge : temp.getNodeEdges(v.individual()))
                    if (simulationChecker.isSimulatedBefore(e, tempEdge, temp, canonicalModel))
                        edgeIsSimulated = true;

                if (edgeIsSimulated)
                    //we Skip adding edge to node V in case that it is simulated by fresh nodes.
                    System.out.println("Edge Skipped");
                else {
                    //add Edge and its successors to the new nodeSet.
                    graphConstructed.addNode(canonicalModel.getNode(e.to()).concept(),
                            canonicalModel.getNode(e.to()).individual());
                    graphConstructed.addNode(v.concept(), v.individual());
                    graphConstructed.addEdge(v.individual(), canonicalModel.getNode(e.to()).individual(),
                            e.property());
                    if (!visited.contains(canonicalModel.getNode(e.to()))) {
                        nonVisited.add(canonicalModel.getNode(e.to()));
                    }
                }
            }

        }

        // }
        if (!nonVisited.isEmpty()) {
            visitNode(nonVisited.poll());
        }
    }

    private void findMinimalGraph(Graph graphConstructed){
        List<Node> nodeSet =graphConstructed.getNodes().stream().toList();
        Set<Edge> edgesToRemove= new HashSet<>();

        for(Node  n: nodeSet) {
            removeRedundancy(graphConstructed, n, edgesToRemove);
        }
        for(Edge e:edgesToRemove) {
          //  System.out.println("Edge: "+e+" is removed.");
            graphConstructed.removeEdge(e);
        }
        if(nodeSet.size()>1) {
            for (Node n : nodeSet)
                if(graphConstructed.getRoot().individual()!=n.individual()) {
                    if(graphConstructed.getSuccessors(n).isEmpty() && graphConstructed.getPredecessors(n).isEmpty()) {
                       // System.out.println("Node: " + n + " is removed.");
                        graphConstructed.removeNode(n);
                    }
                }
        }
    }
    private void removeRedundancy(Graph graphConstructed, Node v,Set<Edge> edgesToRemove) {
        Graph temp = getCopy(graphConstructed, ontology);
        canonicalModelFactory.canonicalFromGraph(temp);
        graphConstructed.printAllPaths(v);
        List<List<Edge>> paths= new ArrayList<>();
        for(List<Edge> p1: graphConstructed.getPaths(v)){
            for(List<Edge> p2: graphConstructed.getPaths(v)){
                if(!p1.isEmpty() && !p2.isEmpty() && !paths.contains(p1))
                if(p1.get(0)!= p2.get(0)){
                    Edge e1= p1.get(0);
                    Edge e2= p2.get(0);
                    if(simulationChecker.isSimulatedBefore(e1,e2,temp,graphConstructed,p1,p2)){
                     //   System.out.print(p1);
                       // System.out.println(" is simulated by: "+p2);
                        paths.add(p1);
                        edgesToRemove.addAll(p1);
                    }
                }
            }
        }

    }
}
