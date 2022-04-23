package CanonicalConstruction;

import GraphLibs.Edge;
import GraphLibs.FreshGraphEntity;
import GraphLibs.Graph;
import GraphLibs.Node;
import Reasoning.ReasonerFacade;
import org.semanticweb.owlapi.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author Mohamed Nadeem
 */
public class CanonicalModelFactory {
    private final OWLOntology ontology;
    private final ReasonerFacade reasoner;
    public CanonicalModelFactory(OWLOntology ontology) throws OWLOntologyCreationException {
        this.ontology=ontology;
        reasoner = new ReasonerFacade(ontology);
    }
    public void canonicalFromIndividual(OWLNamedIndividual owlNamedIndividual,Graph graph){

        addAssertionNodes(owlNamedIndividual,graph);
        addConceptNodes(graph);
        graph.print();

        }
    public void canonicalFromGraph(Graph graph){
     //   addAssertionLoops(graph);
        addConceptNodes(graph);
    }
    private void addConceptNodes(Graph graph){
        Set<OWLClassExpression> classExpressions= new HashSet<>();
        //Collecting all class Expressions.
        for(Node node:graph.getNodes()){
            classExpressions.addAll(node.concept());
        }
        //Selecting all nodes with class expression CE and process it.
        for (OWLClassExpression ce:classExpressions
             ) {
            processNode(ce,graph);
        }
        //Update ClassExpressions on the Node.
        addMissingNodeClassExpressions(graph);

    }
    private void addMissingNodeClassExpressions(Graph graph){
        for(Node node:graph.getFreshNodes()) {
            Set<OWLClassExpression> expressions = new HashSet<>(node.concept());
            for (OWLClassExpression cE : node.concept())
                    expressions.addAll(reasoner.directSubsumeesExcludingOWLNothing(cE));
            System.out.println(node.individual());
            System.out.println(expressions);
            graph.updateNode(expressions,node);
        }
    }
    private void processNode(OWLClassExpression cE,Graph graph){
        Set<OWLClassExpression> subsumees=reasoner.directSubsumeesExcludingOWLNothing(cE);

        for(OWLClassExpression subsumee: subsumees){
            //Creating Fresh Node
            OWLNamedIndividual subject=graph.getFreshGraphEntity().createFreshIndividual(subsumee);
            graph.addNode(Set.of(subsumee),subject);
            Set<Node> subjectsContainingCe = graph.nodesWithConceptExpression(cE);
            boolean notVisitedYet=false;
            subjectsContainingCe.add(graph.getNode(subject));
            if(subsumee instanceof OWLObjectSomeValuesFrom some) {
                // get fillers of subsumee and connect all concepts with Ce to it.
                OWLNamedIndividual object=graph.getFreshGraphEntity().createFreshIndividual(some.getFiller());
                graph.addNode(Set.of(some.getFiller()),object);
                for (Node predcessor:subjectsContainingCe) {
                    if(!graph.edgeExists(predcessor.individual(),object,some.getProperty())) {
                        graph.addEdge(predcessor.individual(),object,some.getProperty());
                        notVisitedYet=true;
                    }
                    
                }
                if(notVisitedYet)
                    processNode(some.getFiller(), graph);
            }
        }
    }
    private void addAssertionNodes(OWLNamedIndividual owlNamedIndividual,Graph graph){


        addAssertionDirectNodes(owlNamedIndividual,graph);

        addAssertionLoops(graph);
    }
    private void addAssertionDirectNodes(OWLNamedIndividual owlNamedIndividual,Graph graph){
        // Works Correctly, checked twice.
        graph.addNode(reasoner.instanceOfExcludingOWLThing(owlNamedIndividual),owlNamedIndividual);
        var individualaxioms = ontology.getObjectPropertyAssertionAxioms(owlNamedIndividual);
        for(OWLObjectPropertyAssertionAxiom axiom:individualaxioms){
            OWLNamedIndividual object=(OWLNamedIndividual) axiom.getObject();
            OWLPropertyExpression property=  axiom.getProperty();
            graph.addNode(reasoner.instanceOfExcludingOWLThing(object),object);
            if(!graph.edgeExists(owlNamedIndividual,object,property)){

                graph.addEdge(owlNamedIndividual,object,property);
                addAssertionDirectNodes(object,graph);
            }
        }
    }
    private void addAssertionLoops(Graph graph){
        Set<Node> Nodes=graph.getNodesExcludingFresh();
        for(Node node : Nodes){
            for (OWLClassExpression cE: node.concept()) {
                Set<OWLClassExpression> subsummees=reasoner.directSubsumeesExcludingOWLNothing(cE);
                for(OWLClassExpression subsumee: subsummees){
                    if(subsumee instanceof OWLObjectSomeValuesFrom some){
                           if(graph.getNodeEdges(node.individual()).stream().noneMatch(e -> e.property().equals(some.getProperty()) && e.to().concept().contains(((OWLObjectSomeValuesFrom) subsumee).getFiller())))
                               graph.addEdge(node.individual(),node.individual(),some.getProperty());
                    }
                }
            }
        }
    }
}
