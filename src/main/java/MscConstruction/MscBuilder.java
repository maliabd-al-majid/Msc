package MscConstruction;

import CanonicalConstruction.CanonicalModelFactory;
import GraphLibs.Edge;
import GraphLibs.Graph;
import GraphLibs.Node;
import SimmulationConstruction.Simulation;

import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.util.*;
/**
 *
 * @author Mohamed Nadeem
 */
public class MscBuilder {
    private final Graph canonicalIndividual, graphConstructed;
    private final OWLOntology ontology;
    private final OWLNamedIndividual owlIndividual;
    private CanonicalModelFactory canonicalModelFactory;
    private LinkedList<Node> visited;
    private LinkedList<Node> nonVisited;
    private Simulation simulationChecker;
    public MscBuilder(OWLOntology ontology,OWLNamedIndividual owlIndividual) throws OWLOntologyCreationException {
        this.ontology=ontology;

        this.owlIndividual=owlIndividual;
        this.canonicalIndividual= new Graph(ontology);
        this.graphConstructed = new Graph(ontology);
        visited= new LinkedList<>();
        nonVisited= new LinkedList<>();
        canonicalModelFactory = new CanonicalModelFactory(ontology);

        simulationChecker=new Simulation();
        //visitNode(graphConstructed.getRoot());

    }
    public void buildMsc(){

        canonicalModelFactory.canonicalFromIndividual(owlIndividual,canonicalIndividual);
        System.out.println("-----------------------------------------");
        System.out.println("- Building MSC");
        System.out.println("-----------------------------------------");
        if(canonicalIndividual.getRoot()!=null) {
            nonVisited.add(canonicalIndividual.getRoot());
            graphConstructed.addNode(canonicalIndividual.getRoot().concept(),canonicalIndividual.getRoot().individual());
        }
        visitNode(nonVisited.poll());
        System.out.println("-----------------------------------------");
        //graphConstructed.print();
    }
    private void visitNode(Node v){
        System.out.println(v);
        if(!visited.contains(v)){
            Graph temp= getCopy(graphConstructed);
            visited.add(v);
            canonicalModelFactory.canonicalFromGraph(temp);
            for(Edge e: canonicalIndividual.getNodeEdgesExcludingFresh(v.individual())){
                if(!simulationChecker.checkSimulation(canonicalIndividual,temp)){
                    //Need to check whether edge already exists
                    boolean edgeIsSimulated=false;
                    for(Edge tempEdge: temp.getNodeEdges(v.individual()))
                    if(simulationChecker.isSimulatedBefore(e,tempEdge,temp,canonicalIndividual))
                        edgeIsSimulated=true;

                    if(edgeIsSimulated)
                        //we Skip adding edge to node V in case that it is simulated by fresh nodes.
                        System.out.println("Edge Skipped");
                    else{
                        //add Edge and its successors to the new nodeSet.
                        graphConstructed.addNode(canonicalIndividual.getNode(e.to()).concept(),canonicalIndividual.getNode(e.to()).individual());
                        graphConstructed.addNode(v.concept(),v.individual());
                        graphConstructed.addEdge(v.individual(),canonicalIndividual.getNode(e.to()).individual(), e.property());
                        if(!visited.contains(canonicalIndividual.getNode(e.to()))) {
                            nonVisited.add(canonicalIndividual.getNode(e.to()));
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
        if(!nonVisited.isEmpty()) {
            visitNode(nonVisited.poll());
        }else {
            //checking Msc Exists or not.
            Graph temp= getCopy(graphConstructed);
            canonicalModelFactory.canonicalFromGraph(temp);
            //graphConstructed.print();
            System.out.println("-----------------------------------------");
            System.out.print("- Decision: ");
            if (simulationChecker.checkSimulation(canonicalIndividual,temp) && simulationChecker.checkSimulation(temp,canonicalIndividual) && !graphConstructed.isCyclic())
                System.out.println("Msc found");
            else
                System.out.println("No Msc found");
        }
    }
    private Graph getCopy(Graph input){
        Graph graph= new Graph(ontology);
        for (Node n: input.getNodes()
             ) {
            graph.addNode(n.concept(),n.individual());
            for(Edge e: input.getNodeEdges(n.individual())) {
                graph.addNode(input.getNode(e.to()).concept(),input.getNode(e.to()).individual());
                graph.addEdge(input.getNode(e.from()).individual(), input.getNode(e.to()).individual(), e.property());
            }
        }

        return graph;

    }

}
