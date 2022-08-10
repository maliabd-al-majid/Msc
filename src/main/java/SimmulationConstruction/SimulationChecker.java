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
    public SimulationChecker(){
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
                //Node has edges that has no correspondence in the constructed one.
                return false;
            }
            else {
                // Now Nodes are simulated, need to check edges.
                for (Edge nodeCanonicalEdge : canonicalModel.getNodeEdges(nodetobeSimulated.individual())) {
                    boolean simulationExists = false;
                    for (Edge nodeConstructedEdge : canonicalConstructed.getNodeEdges(nodeConstructed.individual())){
                        if (checkEdge(nodeCanonicalEdge, nodeConstructedEdge, canonicalModel, canonicalConstructed)) {
                            //System.out.println("sdasdad");
                            //System.out.println(canonicalModel.getNode(nodeCanonicalEdge.to()).individual());
                            //System.out.println(canonicalConstructed.getNode(nodeConstructedEdge.to()).individual());
                            simulationExists = true;
                            Pair pair = new Pair(canonicalModel.getNode(nodeCanonicalEdge.to()).individual(), canonicalConstructed.getNode(nodeConstructedEdge.to()).individual());
                            if (!canonical2Constructed.contains(pair)) {
                                if (!checkNodeEdges(canonicalModel.getNode(nodeCanonicalEdge.to()), canonicalModel, canonicalConstructed.getNode(nodeConstructedEdge.to()), canonicalConstructed, canonical2Constructed)) {
                                    //if there is node in the path that fails to be simulated, then go to the next candidate.
                                    simulationExists = false;
                                }
                                else
                                    break;
                            }
                        }
                    }
                    if (!simulationExists)
                      //  System.out.println(new Pair(nodetobeSimulated.individual(), nodeConstructed.individual()));
                        return false;


                }
                return true;
            }
        } else
            return false;

    }

    private boolean checkNode(Node nodeTobeSimulated, Node node) {
        return node.concept().containsAll(nodeTobeSimulated.concept());
    }
    private boolean checkEdge(Edge nodeCanonicalEdge,Edge nodeConstructedEdge,Graph canonicalModel,Graph canonicalConstructed){
        return nodeCanonicalEdge.property().equals(nodeConstructedEdge.property())
                && checkNode(canonicalModel.getNode(nodeCanonicalEdge.from()), canonicalConstructed.getNode(nodeConstructedEdge.from()))
        && checkNode(canonicalModel.getNode(nodeCanonicalEdge.to()), canonicalConstructed.getNode(nodeConstructedEdge.to()));
    }

    public boolean isSimulatedBefore(Edge edgeToBeSimulated, Edge edge, Graph canonicalConstructed, Graph canonicalModel) {
        if (checkEdge(edgeToBeSimulated,edge,canonicalModel,canonicalConstructed)
        ) {
          //  System.out.println("here");
            return checkNodeEdges(canonicalModel.getNode(edgeToBeSimulated.to()), canonicalModel, canonicalConstructed.getNode(edge.to()), canonicalConstructed, new HashSet<>());
        }
        return false;
    }
    public boolean isSimulatedBefore(Edge edgeToBeSimulated, Edge edge, Graph canonicalConstructed, Graph canonicalModel,List<Edge> pathToBeSimulated, List<Edge> pathConstructed) {
        if (checkEdge(edgeToBeSimulated,edge,canonicalModel,canonicalConstructed)
        ) {
            //  System.out.println("here");
            return checkNodeEdges(canonicalModel.getNode(edgeToBeSimulated.to()), canonicalModel, canonicalConstructed.getNode(edge.to()), canonicalConstructed, new HashSet<>(),pathToBeSimulated,pathConstructed);
        }
        return false;
    }


    private boolean checkNodeEdges(Node nodetobeSimulated,
                                   Graph canonicalModel,
                                   Node nodeConstructed,
                                   Graph canonicalConstructed,
                                   Set<Pair> canonical2Constructed,List<Edge> pathTobeSimulated,List<Edge> pathConstructed) {

        if (checkNode(nodetobeSimulated, nodeConstructed)) {
            //node is simulated, adding the simulated pair
            canonical2Constructed.add(new Pair(nodetobeSimulated.individual(), nodeConstructed.individual()));
            if (!canonicalModel.getSuccessors(nodetobeSimulated).isEmpty()
                    && canonicalConstructed.getSuccessors(nodeConstructed).isEmpty()) {
                //Node has edges that has no correspondence in the constructed one.
                return false;
            }
            else {
                // Now Nodes are simulated, need to check edges.
                List<Edge> nodeCanonicalEdges = new ArrayList<>();
                List<Edge> nodeConstructedEdges = new ArrayList<>();
                for (Edge e1:  canonicalModel.getNodeEdges(nodetobeSimulated.individual()))
                    if(pathTobeSimulated.contains(e1) || canonicalModel.isFresh(canonicalModel.getNode(e1.to()).individual()))
                        nodeCanonicalEdges.add(e1);
                for (Edge e2:  canonicalConstructed.getNodeEdges(nodeConstructed.individual()))
                    if(pathConstructed.contains(e2) || canonicalConstructed.isFresh(canonicalConstructed.getNode(e2.to()).individual()))
                        nodeConstructedEdges.add(e2);
                for (Edge nodeCanonicalEdge : nodeCanonicalEdges) {
                    boolean simulationExists = false;
                    for (Edge nodeConstructedEdge : nodeConstructedEdges){
                        if (checkEdge(nodeCanonicalEdge, nodeConstructedEdge, canonicalModel, canonicalConstructed)) {
                            simulationExists = true;
                            Pair pair = new Pair(canonicalModel.getNode(nodeCanonicalEdge.to()).individual(), canonicalConstructed.getNode(nodeConstructedEdge.to()).individual());
                            if (!canonical2Constructed.contains(pair)) {
                                if (!checkNodeEdges(canonicalModel.getNode(nodeCanonicalEdge.to()), canonicalModel, canonicalConstructed.getNode(nodeConstructedEdge.to()), canonicalConstructed, canonical2Constructed,pathTobeSimulated,pathConstructed)) {
                                    //if there is node in the path that fails to be simulated, then go to the next candidate.
                                    simulationExists = false;
                                }
                                else
                                    break;
                            }
                        }
                    }
                    if (!simulationExists)
                        //  System.out.println(new Pair(nodetobeSimulated.individual(), nodeConstructed.individual()));
                        return false;


                }
                return true;
            }
        } else
            return false;

    }
}
