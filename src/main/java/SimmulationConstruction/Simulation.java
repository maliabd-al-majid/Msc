package SimmulationConstruction;

import GraphLibs.Edge;
import GraphLibs.Graph;
import GraphLibs.Node;
import Utlity.Pair;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLPropertyExpression;

import java.util.*;

public class Simulation {

    public boolean checkSimulation( Graph canonicalIndividual,Graph canonicalConstructed){
       return checkEdge(canonicalIndividual.getRoot(),canonicalIndividual,canonicalConstructed.getRoot(),canonicalConstructed,new HashSet<>());
    }
    private boolean checkEdge(Node nodetobeSimulated, Graph canonicalIndividual, Node nodeConstructed, Graph canonicalConstructed, Set<Pair> canonical2Constructed){
        if(checkNode(nodetobeSimulated,nodeConstructed)) {
            if (!canonicalIndividual.getSuccessors(nodetobeSimulated).isEmpty() && canonicalConstructed.getSuccessors(nodeConstructed).isEmpty())
                return false;
            else {
                canonical2Constructed.add(new Pair(nodetobeSimulated.individual(), nodeConstructed.individual()));
                for (Edge nodeCanonicalEdge : canonicalIndividual.getNodeEdges(nodetobeSimulated.individual())) {
                    boolean simulateEdgeExists = false;
                    for (Edge nodeConstructedEdge : canonicalConstructed.getNodeEdges(nodeConstructed.individual()))
                        if (nodeCanonicalEdge.property().equals(nodeConstructedEdge.property()) && checkNode(canonicalIndividual.getNode(nodeCanonicalEdge.to()), canonicalConstructed.getNode(nodeConstructedEdge.to()))) {
                            simulateEdgeExists = true;
                            Pair pair = new Pair(canonicalIndividual.getNode(nodeCanonicalEdge.to()).individual(), canonicalConstructed.getNode(nodeConstructedEdge.to()).individual());
                            if (!canonical2Constructed.contains(pair))
                               return checkEdge(canonicalIndividual.getNode(nodeCanonicalEdge.to()), canonicalIndividual, canonicalConstructed.getNode(nodeConstructedEdge.to()), canonicalConstructed, canonical2Constructed);
                        }
                    if (!simulateEdgeExists)
                        return false;
                }
                return true;
            }
        }
        else return false;
    }
    private boolean checkNode(Node nodeTobeSimulated,Node node){
        if(node.concept().containsAll(nodeTobeSimulated.concept()))
        return true;
        return false;
    }
    public boolean isSimulatedBefore(Edge edgeToBeSimulated, Edge edge,Graph canonicalConstructed,Graph canonicalIndividual){
        //Set<OWLClassExpression> conceptsToBesimulated=new HashSet<>();

       // Set<Node> successorNodesConstructed=canonicalConstructed.getSuccessors(canonicalConstructed.getNode(edge.to()));
       // Set<Node> successorNodestobeSimulated= canonicalIndividual.getSuccessors(canonicalIndividual.getNode(edgeToBeSimulated.to()));
       // for (Node n: successorNodestobeSimulated)
         //   conceptsToBesimulated.addAll(n.concept());
        //Set<OWLClassExpression> conceptsConstructed=new HashSet<>();
       // for (Node n: successorNodesConstructed)
         //   conceptsConstructed.addAll(n.concept());
        if(canonicalConstructed.getNode(edge.from()).concept().containsAll(canonicalIndividual.getNode(edgeToBeSimulated.from()).concept())
        && canonicalConstructed.getNode(edge.to()).concept().containsAll(canonicalIndividual.getNode(edgeToBeSimulated.to()).concept())
        && edge.property().equals(edgeToBeSimulated.property())
        ) {
            return checkEdge(canonicalIndividual.getNode(edgeToBeSimulated.to()),canonicalIndividual,canonicalConstructed.getNode(edge.to()),canonicalConstructed,new HashSet<>());
        }
return false;
    }
}
