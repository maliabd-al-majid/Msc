package SimmulationConstruction;

import GraphLibs.Edge;
import GraphLibs.Graph;
import GraphLibs.Node;
import Utlity.Pair;

import java.util.*;
/**
 *
 * @author Mohamed Nadeem
 */
public class SimulationChecker {
    Set<Pair> failedPairs;
    public SimulationChecker(){
        failedPairs=new HashSet<>();
    }
    public boolean checkSimulation(Graph canonicalModel, Graph canonicalConstructed) {
        return checkNodeEdges(canonicalModel.getRoot(), canonicalModel, canonicalConstructed.getRoot(), canonicalConstructed, new HashSet<>());
    }

    private boolean checkNodeEdges(Node nodetobeSimulated,
                                   Graph canonicalModel,
                                   Node nodeConstructed,
                                   Graph canonicalConstructed,
                                   Set<Pair> canonical2Constructed) {

        if (checkNode(nodetobeSimulated, nodeConstructed)) {
            //node is simulated, adding the simulated pair
            canonical2Constructed.add(new Pair(nodetobeSimulated.individual(), nodeConstructed.individual()));
            if (!canonicalModel.getSuccessors(nodetobeSimulated).isEmpty()
                    && canonicalConstructed.getSuccessors(nodeConstructed).isEmpty()) {
                System.out.println(new Pair(nodetobeSimulated.individual(), nodeConstructed.individual()));
                //Node has edges that has no correspondence in the constructed one.
                return false;
            }
            else {
                // Now Nodes are simulated, need to check edges.
                for (Edge nodeCanonicalEdge : canonicalModel.getNodeEdges(nodetobeSimulated.individual())) {
                    boolean simulateEdgeExists = false;
                    for (Edge nodeConstructedEdge : canonicalConstructed.getNodeEdges(nodeConstructed.individual())){
                        if (checkEdge(nodeCanonicalEdge, nodeConstructedEdge, canonicalModel, canonicalConstructed)) {
                            simulateEdgeExists = true;
                            Pair pair = new Pair(canonicalModel.getNode(nodeCanonicalEdge.to()).individual(), canonicalConstructed.getNode(nodeConstructedEdge.to()).individual());
                            if (!canonical2Constructed.contains(pair)) {
                                if (!checkNodeEdges(canonicalModel.getNode(nodeCanonicalEdge.to()), canonicalModel, canonicalConstructed.getNode(nodeConstructedEdge.to()), canonicalConstructed, canonical2Constructed)) {
                                    //if there is node in the path that fails to be simulated, then go to the next candidate.
                                    failedPairs.add(pair);
                             //       System.out.println(failedPairs);
                                    simulateEdgeExists = false;
                                }
                                else
                                    break;
                            }
                        }
                    }
                    if (!simulateEdgeExists) {
                      //  System.out.println(new Pair(nodetobeSimulated.individual(), nodeConstructed.individual()));
                        return false;
                    }

                }
                return true;
            }
        } else {
           // System.out.println(new Pair(nodetobeSimulated.individual(), nodeConstructed.individual()));
            //System.out.println("NOO");
            return false;
        }
    }

    private boolean checkNode(Node nodeTobeSimulated, Node node) {
        return node.concept().containsAll(nodeTobeSimulated.concept());
    }
    private boolean checkEdge(Edge nodeCanonicalEdge,Edge nodeConstructedEdge,Graph canonicalModel,Graph canonicalConstructed){
        return nodeCanonicalEdge.property().equals(nodeConstructedEdge.property()) && checkNode(canonicalModel.getNode(nodeCanonicalEdge.to()), canonicalConstructed.getNode(nodeConstructedEdge.to()));
    }

    public boolean isSimulatedBefore(Edge edgeToBeSimulated, Edge edge, Graph canonicalConstructed, Graph canonicalModel) {
        if (canonicalConstructed.getNode(edge.from()).concept().containsAll(canonicalModel.getNode(edgeToBeSimulated.from()).concept())
                && canonicalConstructed.getNode(edge.to()).concept().containsAll(canonicalModel.getNode(edgeToBeSimulated.to()).concept())
                && edge.property().equals(edgeToBeSimulated.property())
        ) {
            return checkNodeEdges(canonicalModel.getNode(edgeToBeSimulated.to()), canonicalModel, canonicalConstructed.getNode(edge.to()), canonicalConstructed, new HashSet<>());
        }
        return false;
    }
}
