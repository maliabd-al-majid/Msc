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
    private final Map<Integer,List<List<Edge>>> Paths=new HashMap<>();
    private int nodesNo=0;
    private final FreshGraphEntity freshGraphEntity;
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
            Paths.putIfAbsent(nodesNo,new ArrayList<>());
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
    public Node getNode(int nodesNo){
        return nodes.get(nodesNo);
    }
    public Node getNode(OWLNamedIndividual individual) {
        for (Node n: nodes.values()){
            if(n.individual().equals(individual))
                return n;
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
            if(adjacencyList.get(from.label()).get(i).to()==to.label() && adjacencyList.get(from.label()).get(i).property().equals(prperty))
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
            if(adjacencyList.get(from.label()).get(i).to()==to.label())
                return true;
        }
        return false;
    }
    public boolean edgeExists(OWLNamedIndividual subject,Set<OWLClassExpression> concepts, OWLPropertyExpression property ){
       if(!nodeExists(subject))
           return false;
       else {
           var x=getNodeEdges(subject);
           for(Edge e:getNodeEdges(subject)) {

               if (e.property().equals(property) && nodes.get(e.to()).concept().equals(concepts))
                   return true;
           }
           return false;
       }
    }

    public List<Edge> getNodeEdges(OWLNamedIndividual owlNamedIndividual){

        return adjacencyList.get(Objects.requireNonNull(getNode(owlNamedIndividual)).label());
    }
    public List<Edge> getNodeEdgesExcludingFresh(OWLNamedIndividual owlNamedIndividual){
        List<Edge> edges= new ArrayList<>();
        for(Edge edge:adjacencyList.get(Objects.requireNonNull(getNode(owlNamedIndividual)).label()))
            if(!isFresh(nodes.get(edge.to()).individual()))
                edges.add(edge);
        return edges;
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
    public boolean isFresh(OWLNamedIndividual node){
      //  System.out.println("Node "+ node);
     //   System.out.println(getFreshNodes());
        //System.out.println("fresh");
        return getFreshNodes().contains(getNode(node));
        //System.out.println("NOT FRESH");
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
            successors.add(nodes.get(edge.to()));
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
            if(adjacencyList.get(i)!=null)
            for(Edge edge: adjacencyList.get(i))
                if(edge.to()==currentNode.label())
                    predecessors.add(nodes.get(edge.from()));
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

            adjacencyList.get(fromNode.label()).add(new Edge(fromNode.label(), toNode.label(), property));
        }
    }
    public void removeEdge(Edge edge){
        //System.out.println(adjacencyList);
        adjacencyList.get(edge.from()).remove(edge);
    }
    public void removeNode(Node node){

            adjacencyList.remove(node.label());
            nodes.remove(node.label());

    }
    public void printAllPaths(Node s)
    {

        //System.out.println(s);
        boolean[] isVisited = new boolean[nodesNo];
        ArrayList<Node> pathList = new ArrayList<>();

        // add source to path[]
        pathList.add(s);
        // Call recursive utility
        printAllPathsUtil(s, isVisited, pathList);
        //System.out.println(allPaths);
    }

    // A recursive function to print
    // all paths from 'u' to 'd'.
    // isVisited[] keeps track of
    // vertices in current path.
    // localPathList<> stores actual
    // vertices in the current path

    public int getGraphDepth(){
        int num=-1;
        List<List<Edge>> paths=getPaths(getRoot());
        if(!paths.isEmpty())
        for(List<Edge> p:paths)
            if(p.size()>num)
                num=p.size();
        return num;
    }
    private void printAllPathsUtil(Node u,
                                   boolean[] isVisited,
                                   List<Node> localPathList)
    {
        isVisited[u.label()] = true;
        for (Node i : getSuccessors(u)) {
            if (!isVisited[i.label()]) {
                localPathList.add(i);
                printAllPathsUtil(i, isVisited, localPathList);

                localPathList.remove(i);
            }else {
                //Collect Cyclic Paths
                List<Node> collectedPath = new ArrayList<>(localPathList);
                collectedPath.add(i);
               // System.out.println(collectedPath);
                    List<Edge> pathEdges= new ArrayList<>();
                    for (int x = 0; x < collectedPath.size() - 1; x++) {
                        List<Edge> edges = getNodeEdges(collectedPath.get(x).individual());
                        for (Edge edge : edges)
                            if (getNode(edge.to()) == collectedPath.get(x + 1)) {
                                pathEdges.add(edge);
                            }
                    }
                    Paths.get(localPathList.get(0).label()).add(pathEdges);
                   // pathsEdges.add(pathEdges);


              //  Paths.get(0).
              //  allPaths.add(collectedPath);
            }
        }
        if(getSuccessors(u).size()==0){
            List<Edge> pathEdges= new ArrayList<>();
            for (int x = 0; x < localPathList.size() - 1; x++) {
                List<Edge> edges = getNodeEdges(localPathList.get(x).individual());
                for (Edge edge : edges)
                    if (getNode(edge.to()) == localPathList.get(x + 1)) {
                        pathEdges.add(edge);
                    }
            }

            Paths.get(localPathList.get(0).label()).add(pathEdges);
          //  allPaths.add(localPathList);
        }
        isVisited[u.label()] = false;
    }
    public List<List<Edge>> getPaths(Node n){
        return Paths.get(n.label());
    }
}