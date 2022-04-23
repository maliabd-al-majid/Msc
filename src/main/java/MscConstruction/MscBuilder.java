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
    private final List<Node> visited;
    private List<Node> nonVisited;
    private Simulation simulationChecker;
    public MscBuilder(OWLOntology ontology,OWLNamedIndividual owlIndividual) throws OWLOntologyCreationException {
        this.ontology=ontology;

        this.owlIndividual=owlIndividual;
        this.canonicalIndividual= new Graph(ontology);
        this.graphConstructed = new Graph(ontology);
        simulationChecker=new Simulation();
        visited= new ArrayList<>();
        nonVisited= new ArrayList<>();
         canonicalModelFactory = new CanonicalModelFactory(ontology);
        canonicalModelFactory.canonicalFromIndividual(owlIndividual,canonicalIndividual);
    }
    public void buildMsc(){
        //TODO Traverse Graph in BFS, Ignore Fresh nodes in CanonicalIndividual, Test construction of Msc

        if(canonicalIndividual.getRoot()!= null){

            graphConstructed.addNode(canonicalIndividual.getRoot().concept(),canonicalIndividual.getRoot().individual());
            nonVisited.add(graphConstructed.getRoot());
            visitNode(graphConstructed.getRoot());
            Node currLevelVisitNode= graphConstructed.getRoot();

            while (!nonVisited.isEmpty()){
                Set<Node> successorLevel= graphConstructed.getSuccessors(currLevelVisitNode);
                System.out.println("Successors before "+successorLevel);
                for (Node n: visited
                     ) {
                    successorLevel.remove(n);
                }
                System.out.println("Successors after "+successorLevel);
                if(!successorLevel.isEmpty()){
                    visitNode(successorLevel.stream().toList().get(0));
            }else {
                    currLevelVisitNode=nonVisited.get(0);
                }

            }

        }
    }
    private void visitNode(Node v){

        if(!visited.contains(v)){
            Graph temp= graphConstructed;
            for(Edge e: canonicalIndividual.getNodeEdges(v.individual())){

                canonicalModelFactory.canonicalFromGraph(temp);
                visited.add(v);

                if(!simulationChecker.checkSimulation(canonicalIndividual,temp)){
                    //Need to check whether edge already exists
                    if(graphConstructed.edgeExists(v.individual(),e.to().individual(), e.property())){
                        //No Msc
                        nonVisited.remove(v);
                        return;
                    }
                    else{
                        //add Edge and its successors to the new nodeSet.
                        graphConstructed.addNode(e.to().concept(),e.to().individual());
                        graphConstructed.addEdge(v.individual(),e.to().individual(), e.property());
                        if(!visited.contains(e.to()))
                            nonVisited.add(e.to());
                    }
                }
                else {
                    //Msc found
                    System.out.println("Msc Found");
                    System.out.println(graphConstructed);
                }
            }
            nonVisited.remove(v);
        }


    }

}
