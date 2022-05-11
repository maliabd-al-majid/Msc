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

import static Utlity.GraphUtility.*;

/**
 * @author Mohamed Nadeem
 */
public class MscBuilderFactory {
    private final Graph canonicalModel, graphConstructed;
    private final OWLOntology ontology;
    private final OWLNamedIndividual owlIndividual;
    private final CanonicalModelFactory canonicalModelFactory;
    private final LinkedList<Node> visited;
    private final LinkedList<Node> nonVisited;
    private final SimulationChecker simulationChecker;

    public Graph getGraphConstructed() {
        return graphConstructed;
    }
    public Graph getCanonicalGraphConstructed(){
        Graph temp = getCopy(graphConstructed,ontology);
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
        System.out.println("- Building MSC");
        System.out.println("-----------------------------------------");
        if (canonicalModel.getRoot() != null) {
            nonVisited.add(canonicalModel.getRoot());
            graphConstructed.addNode(canonicalModel.getRoot().concept(), canonicalModel.getRoot().individual());
            visitNode(nonVisited.poll());
        } else {
            System.out.println(" Canonical Model of Individual is Empty");
            return false;
        }

        System.out.println("-----------------------------------------");
            //checking whether Msc Exists or not.
            Graph temp = getCopy(graphConstructed,ontology);
            canonicalModelFactory.canonicalFromGraph(temp);
            return simulationChecker.checkSimulation(canonicalModel, temp) && simulationChecker.checkSimulation(temp, canonicalModel) && !isCyclic(graphConstructed);

    }

    private void visitNode(Node v) {
        System.out.println(v);
        if (!visited.contains(v)) {
            Graph temp = getCopy(graphConstructed,ontology);
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

            }

        }
         if (!nonVisited.isEmpty()) {
            visitNode(nonVisited.poll());
        }
    }



}
