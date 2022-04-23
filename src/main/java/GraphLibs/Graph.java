package GraphLibs;

import com.google.common.collect.Sets;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
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
    private FreshGraphEntity freshGraphEntity;
    public int getNumberOfNodes() {
        return nodesNo;
    }
    public Graph(OWLOntology ontology){
        freshGraphEntity=new FreshGraphEntity(ontology);
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
    public void updateNode(Set<OWLClassExpression> concept,Node node){
        if(nodeExists(node.individual())){
            concept.addAll(node.concept());
            Node updatedNode = new Node(node.label(),node.individual(),concept);
            nodes.replace(node.label(),updatedNode);
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

    public Node getNode(OWLNamedIndividual individual) {
        for (int i=0; i<nodesNo;i++){
            if(nodes.get(i).individual().equals(individual))
                return nodes.get(i);
        }
        return null;
    }
    public OWLPropertyExpression getEdge(OWLNamedIndividual subject,OWLNamedIndividual object){
       List<Edge> edges= getNodeEdges(subject);
       for (Edge edge:edges){
           if(edge.to().individual().equals(object))
               return edge.property();
       }
       return null;
    }
    public boolean edgeExists(OWLNamedIndividual subject,OWLNamedIndividual object,OWLPropertyExpression prperty){
        Node from=null;
        Node to=null;
        try {
             from=nodes.get(Objects.requireNonNull(getNode(subject)).label());
             to=nodes.get(Objects.requireNonNull(getNode(object)).label());
        }
catch (Exception e) {
    return false;
}
        for(int i = 0; i< adjacencyList.get(from.label()).size(); i++){
            if(adjacencyList.get(from.label()).get(i).to().label()==to.label() && adjacencyList.get(from.label()).get(i).property().equals(prperty))
                return true;
        }
        return false;
    }
    public boolean edgeExists(OWLNamedIndividual subject,OWLNamedIndividual object ){
        Node from=null;
        Node to=null;
        try {
             from=nodes.get(Objects.requireNonNull(getNode(subject)).label());
             to=nodes.get(Objects.requireNonNull(getNode(object)).label());
        }
catch (Exception e) {
    return false;
}
        for(int i = 0; i< adjacencyList.get(from.label()).size(); i++){
            if(adjacencyList.get(from.label()).get(i).to().label()==to.label())
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
            if(adjacencyList.get(i).isEmpty() || edgeExists(nodes.get(i).individual(),nodes.get(i).individual()))
                leaves.add(nodes.get(i));
        return leaves;
    }
    public Set<Node> getFreshNodes(){
        return (getNodes().stream().filter(c-> freshGraphEntity.getClass2ind().containsValue(c.individual()))).collect(Collectors.toSet());

    }
    public boolean isFresh(Node node){
        return getFreshNodes().contains(node);
    }
    public Set<Node> getLeafNodesExcludingFresh(){

        return  (getLeafNodes().stream().filter(c-> !freshGraphEntity.getClass2ind().containsValue(c.individual()))).collect(Collectors.toSet());
    }

    public FreshGraphEntity getFreshGraphEntity() {
        return freshGraphEntity;
    }

    public Set<Node> getNodesExcludingFresh(){
        return  (getNodes().stream().filter(c-> !freshGraphEntity.getClass2ind().containsValue(c.individual()))).collect(Collectors.toSet());
    }
    public Set<Node> getSuccessors(Node currentNode){
        Set<Node> successors= new HashSet<>();
        for(Edge edge:adjacencyList.get(currentNode.label()))
            successors.add(edge.to());
        return successors;
    }
    public Set<Node> getallSuccessors(Node currentNode, Set<Node> allsuccessors){
        if(allsuccessors==null)
            allsuccessors= new HashSet<>();
        for(Node successor:getSuccessors(currentNode)){
            if(!allsuccessors.contains(successor)){
                allsuccessors.add(successor);
                getallSuccessors(successor,allsuccessors);
            }
        }
        return allsuccessors;
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
        if(nodes.size()>0)
        return nodes.get(0);
        return null;
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
        System.out.println("- Canonical Graph:");
        for (var node : nodes.values()) {
            System.out.println(node+": Concepts("+node.concept()+")"+", Individual:"+node.individual());
            System.out.println("Edges: " + adjacencyList.get(node.label()));
        }
    }




}