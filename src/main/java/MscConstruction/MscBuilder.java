package MscConstruction;

import CanonicalConstruction.CanonicalModelFactory;
import GraphLibs.Edge;
import GraphLibs.Graph;
import GraphLibs.Node;
import SimmulationConstruction.SimulationChecker;

import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.util.*;

/**
 * @author Mohamed Nadeem
 */
public class MscBuilder {
    private final Graph canonicalModel, graphConstructed;
    private final OWLOntology ontology;
    private final OWLNamedIndividual owlIndividual;
    private CanonicalModelFactory canonicalModelFactory;
    private LinkedList<Node> visited;
    private LinkedList<Node> nonVisited;
    private SimulationChecker simulationChecker;

    public MscBuilder(OWLOntology ontology, OWLNamedIndividual owlIndividual) throws OWLOntologyCreationException {
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

    public void buildMsc() {

        canonicalModelFactory.canonicalFromIndividual(owlIndividual, canonicalModel);
        System.out.println("-----------------------------------------");
        System.out.println("- Building MSC");
        System.out.println("-----------------------------------------");
        if (canonicalModel.getRoot() != null) {
            nonVisited.add(canonicalModel.getRoot());
            graphConstructed.addNode(canonicalModel.getRoot().concept(), canonicalModel.getRoot().individual());
            visitNode(nonVisited.poll());
        } else
            System.out.println(" Canonical Model of Individual is Empty");

        System.out.println("-----------------------------------------");
        //graphConstructed.print();
    }

    private void visitNode(Node v) {
        System.out.println(v);
        if (!visited.contains(v)) {
            Graph temp = getCopy(graphConstructed);
            visited.add(v);
            canonicalModelFactory.canonicalFromGraph(temp);
            for (Edge e : canonicalModel.getNodeEdgesExcludingFresh(v.individual())) {
                if (!simulationChecker.checkSimulation(canonicalModel, temp)) {
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
                //   else {
                //Msc found
                //   System.out.println("Msc Found");
                //  System.out.println(graphConstructed);
                //}
            }

        }
        if (!nonVisited.isEmpty()) {
            visitNode(nonVisited.poll());
        } else {
            //checking Msc Exists or not.
            Graph temp = getCopy(graphConstructed);
          //  temp.print();
            canonicalModelFactory.canonicalFromGraph(temp);
           // graphConstructed.print();
            System.out.println("-----------------------------------------");
            System.out.print("- Decision: ");
            if (simulationChecker.checkSimulation(canonicalModel, temp) && simulationChecker.checkSimulation(temp, canonicalModel) && !graphConstructed.isCyclic()) {
                System.out.println("Msc found");
                temp.print();
            } else {

                System.out.println("No Msc found");
               // temp.print();
            }
        }
    }

    private Graph getCopy(Graph input) {
        Graph graph = new Graph(ontology);
        for (int i=0;i<input.getNumberOfNodes();i++
        ) {

            graph.addNode(input.getNode(i).concept(), input.getNode(i).individual());
            for (Edge e : input.getNodeEdges(input.getNode(i).individual())) {
                graph.addNode(input.getNode(e.to()).concept(), input.getNode(e.to()).individual());
                graph.addEdge(input.getNode(e.from()).individual(), input.getNode(e.to()).individual(), e.property());
            }
        }

        return graph;

    }

}
