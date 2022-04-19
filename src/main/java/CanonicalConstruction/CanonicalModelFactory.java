package CanonicalConstruction;

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
    private OWLOntology ontology;
    private OWLOntology resultOntology;// need it for future Use.

    private final ReasonerFacade reasoner;
    private final FreshGraphEntity freshGraphEntity;
    public CanonicalModelFactory(OWLOntology ontology) throws OWLOntologyCreationException {
        this.freshGraphEntity=new FreshGraphEntity(ontology);
        this.ontology=ontology;
        resultOntology= ontology.getOWLOntologyManager().createOntology();
        ontology.getOWLOntologyManager().getOWLDataFactory();
        reasoner = new ReasonerFacade(ontology);
    }
    public void canonicalFromIndividual(OWLNamedIndividual owlNamedIndividual,Graph graph){

        addAssertionNodes(owlNamedIndividual,graph);
    //    System.out.println("Before Considering TBOX");
      //  graph.print();
    //    System.out.println("After Considering TBOX to ABOX");
        addConceptNodes(graph);
        graph.print();

        }
    public void canonicalFromGraph(Graph graph){
        addAssertionLoops(graph);
        addConceptNodes(graph);
    }


    private void addConceptNodes(Graph graph){

        System.out.println(ontology.getOntologyID().getOntologyIRI());
        for(Node node:graph.getNodes()){
                //System.out.println("Concepts: "+node.concept());
                for(OWLClassExpression conceptExperession: node.concept()) {
                    processNode(conceptExperession,graph);
                }
        }
    }
    private void processNode(OWLClassExpression cE,Graph graph){


        Set<OWLClassExpression> fillers=reasoner.directSubsumeesExcludingOWLNothing(cE);


            //A1 or A2
            //
            for(OWLClassExpression currFiller:fillers){
                OWLNamedIndividual subject=freshGraphEntity.createFreshIndividual(currFiller);
                graph.addNode(Set.of(currFiller),subject);
                if(currFiller instanceof OWLObjectSomeValuesFrom some){

                    OWLNamedIndividual object=freshGraphEntity.createFreshIndividual(some.getFiller());
                    graph.addNode(Set.of(some.getFiller()),object);
                    if(!graph.edgeExists(subject,object,freshGraphEntity.createFreshPropertyExpression())) {
                        graph.addEdge(subject,object,freshGraphEntity.createFreshPropertyExpression());
                        processNode(some.getFiller(),graph);
                    }else{
                        if(!graph.edgeExists(object,object,freshGraphEntity.createFreshPropertyExpression())) {
                            graph.addEdge(object,object,freshGraphEntity.createFreshPropertyExpression());
                        }
                    }
                }
                 //   graph.addEdge();
            }



         if (cE instanceof OWLObjectSomeValuesFrom some){
            OWLNamedIndividual subject=freshGraphEntity.createFreshIndividual(cE);
            graph.addNode(Set.of(cE),subject);
            OWLNamedIndividual object=freshGraphEntity.createFreshIndividual(some.getFiller());
            graph.addNode(Set.of(some.getFiller()),object);
          //  if(!graph.edgeExists(subject,object,freshGraphEntity.createFreshPropertyExpression())) {
                graph.addEdge(subject,object,freshGraphEntity.createFreshPropertyExpression());
                processNode(some.getFiller(),graph);
            //}
        }

        //adding Edges from individual to concept Nodes.
        for(Node n: graph.nodesWithConceptExpression(cE)) {

            OWLNamedIndividual object=freshGraphEntity.createFreshIndividual(cE);
            if(!n.individual().equals(object)) {
                graph.addNode(Set.of(cE), object);
                graph.addEdge(n.individual(), object, freshGraphEntity.createFreshPropertyExpression());
            }
        }
    }
    private void addAssertionNodes(OWLNamedIndividual owlNamedIndividual,Graph graph){
        addAssertionDirectNodes(owlNamedIndividual,graph);
        addAssertionLoops(graph);
    }
    private void addAssertionDirectNodes(OWLNamedIndividual owlNamedIndividual,Graph graph){
        // Works Correctly, checked twice.
        var currentLevelIndividuals=ontology.getAxioms(owlNamedIndividual);
        for (OWLAxiom axiom:currentLevelIndividuals) {
            if (axiom instanceof OWLClassAssertionAxiom ) {
                graph.addNode(getIndividualClass(owlNamedIndividual), owlNamedIndividual);
            } else if (axiom instanceof OWLObjectPropertyAssertionAxiom) {
                OWLNamedIndividual subject=(OWLNamedIndividual) ((OWLObjectPropertyAssertionAxiom) axiom).getSubject();
                OWLNamedIndividual object=(OWLNamedIndividual) ((OWLObjectPropertyAssertionAxiom) axiom).getObject();
                OWLPropertyExpression property= ((OWLObjectPropertyAssertionAxiom) axiom).getProperty();
                //System.out.println("property assertion" + axiom);
                graph.addNode(getIndividualClass(subject), subject);
                graph.addNode(getIndividualClass(object),object);
                if(!graph.edgeExists(subject,object,property)){

                    graph.addEdge(subject,object,property);
                    addAssertionDirectNodes(object,graph);
                }
                // System.out.println (((OWLObjectPropertyAssertionAxiom) axiom).asOWLSubClassOfAxiom().getSubClass());
            }
        }
    }
    private void addAssertionLoops(Graph graph){
        Set<Node> leafNodes=graph.getLeafNodesExcludingFresh(freshGraphEntity.getClass2ind());
        for(Node leaf : leafNodes){
            for (OWLClassExpression cE: leaf.concept()) {
                Set<OWLClassExpression> fillers=reasoner.directSubsumeesExcludingOWLNothing(cE);
                for(OWLClassExpression filler: fillers){
                    if(filler instanceof OWLObjectSomeValuesFrom some){
                        graph.addEdge(leaf.individual(),leaf.individual(),some.getProperty());
                    }
                }
            }
        }
    }
    private Set<OWLClassExpression> getIndividualClass(OWLNamedIndividual owlNamedIndividual){
        Set<OWLClassExpression> owlClassExpressions=new HashSet<>();
        var  classAxioms=ontology.getClassAssertionAxioms(owlNamedIndividual);
        for(OWLClassAssertionAxiom classAssertionAxiom:classAxioms){
            owlClassExpressions.add(classAssertionAxiom.getClassExpression());
        }
        return owlClassExpressions;
    }
}
