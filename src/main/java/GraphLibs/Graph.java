package GraphLibs;

import com.google.common.collect.Sets;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLPropertyExpression;

import java.util.*;
import java.util.stream.Collectors;
/**
 *
 * @author Mohamed Nadeem
 */
public class Graph {
    private final Map<Integer, Node> nodes = new HashMap<>();
    private final Map<Integer, List<Edge>> adjacencyList = new HashMap<>();
    private int nodesNo=0;

    public int getNumberOfNodes() {
        return nodesNo;
    }

    public Set<Node> getNodes() {
        return Sets.newHashSet(nodes.values());
    }

    public void addNode(Set<OWLClassExpression> concept, OWLNamedIndividual individual) {
        if (!nodeExists(individual)) {
            var node = new Node(nodesNo, individual, concept);
            nodes.putIfAbsent(nodesNo, node);
            adjacencyList.putIfAbsent(nodesNo, new ArrayList<>());
            nodesNo++;
        }
    }

    public boolean nodeExists(OWLNamedIndividual individual) {
        for (int i=0; i<nodesNo;i++){
            if(nodes.get(i).individual().equals(individual))
                return true;
        }
        return false;
    }
    public Map<Integer, List<Edge>> getAdjacencyList() {
        return adjacencyList;
    }

    private Node getNode(OWLNamedIndividual individual) {
        for (int i=0; i<nodesNo;i++){
            if(nodes.get(i).individual().equals(individual))
                return nodes.get(i);
        }
        return null;
    }
    public Node getNode(int nodeNo){
        return nodes.get(nodeNo);
    }
    public boolean edgeExists(OWLNamedIndividual subject,OWLNamedIndividual object,OWLPropertyExpression prperty){
        Node from=nodes.get(Objects.requireNonNull(getNode(subject)).label());
        Node to=nodes.get(Objects.requireNonNull(getNode(object)).label());
        for(int i = 0; i< adjacencyList.get(from.label()).size(); i++){
            if(adjacencyList.get(from.label()).get(i).to().label()==to.label() && adjacencyList.get(from.label()).get(i).property().equals(prperty))
                return true;
        }
        return false;
    }
    public List<Edge> getNodeEdges(OWLNamedIndividual owlNamedIndividual){
        return adjacencyList.get(Objects.requireNonNull(getNode(owlNamedIndividual)).label());
    }
    public Set<Node> getLeafNodes(){
        Set<Node> leaves= new HashSet<>();
        for(int i=0;i<nodesNo;i++)
            if(adjacencyList.get(i).isEmpty())
                leaves.add(nodes.get(i));
        return leaves;
    }
    public Set<Node> getLeafNodesExcludingFresh(Map<OWLClassExpression,OWLNamedIndividual> class2ind){

        return  (getLeafNodes().stream().filter(c-> !class2ind.containsValue(c.individual()))).collect(Collectors.toSet());
    }
    public Set<Node> getSuccessors(Node currentNode){
        Set<Node> successors= new HashSet<>();
        for(Edge edge:adjacencyList.get(currentNode.label()))
            successors.add(edge.to());
        return successors;
    }
    public Set<Node> getPredecessors(Node currentNode){
        Set<Node> predecessors= new HashSet<>();
        for(int i=0;i< nodesNo;i++)
            for(Edge edge: adjacencyList.get(i))
                if(edge.to().equals(currentNode))
                    predecessors.add(edge.from());
        return predecessors;
    }
    public Node getRoot(){
        return nodes.get(0);
    }
    public Set<Node> nodesWithConceptExpression(OWLClassExpression cE){
        Set<Node> nodeSet=new HashSet<>();
        for(int i=0;i<nodesNo;i++){
            if(nodes.get(i).concept().contains(cE))
                nodeSet.add(nodes.get(i));
        }
        return nodeSet;
    }
    public void addEdge(OWLNamedIndividual subject, OWLNamedIndividual object, OWLPropertyExpression property) {
        if(!edgeExists(subject,object,property)) {
            var fromNode = nodes.get(Objects.requireNonNull(getNode(subject)).label());
            if (fromNode == null) throw new IllegalArgumentException();

            var toNode = nodes.get(Objects.requireNonNull(getNode(object)).label());
            if (toNode == null) throw new IllegalArgumentException();

            adjacencyList.get(fromNode.label()).add(new Edge(fromNode, toNode, property));
        }
    }
    public void print() {
        System.out.println("-Graph:");
        for (var node : nodes.values()) {
            System.out.println(node+": Concepts("+node.concept()+")"+", Individual:"+node.individual());
            System.out.println("Edges: " + adjacencyList.get(node.label()));
        }
    }




}