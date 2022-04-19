package GraphLibs;

import org.semanticweb.owlapi.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FreshGraphEntity {
    private final Map<OWLClassExpression, OWLNamedIndividual> class2ind;
    private int fresh_individual_index=0;
    private OWLOntology ontology;
    private final OWLDataFactory factory;
    public Map<OWLClassExpression, OWLNamedIndividual> getClass2ind() {
        return class2ind;
    }
    public FreshGraphEntity(OWLOntology ontology){
        class2ind= new HashMap<>();
        this.ontology=ontology;
        this.factory = ontology.getOWLOntologyManager().getOWLDataFactory();
    }
    public OWLNamedIndividual createFreshIndividual(OWLClassExpression expression){
        if(!class2ind.containsKey(expression)) {
            String FRESH_Individual_PREFIX = "Fresh_Individual_";
            OWLNamedIndividual owlNamedIndividual = factory.getOWLNamedIndividual(IRI.create(ontology.getOntologyID().getOntologyIRI().get() + "#"+ FRESH_Individual_PREFIX +fresh_individual_index));
            class2ind.putIfAbsent(expression, owlNamedIndividual);
            fresh_individual_index++;
            return owlNamedIndividual;
        }else return class2ind.get(expression);
    }
    public OWLNamedIndividual getFreshIndividual(OWLClassExpression expression){
        return class2ind.get(expression);
    }
    public boolean individualExists(OWLClassExpression expression){
        return class2ind.containsKey(expression);
    }
    public OWLPropertyExpression createFreshPropertyExpression(){
        String FRESH_Property_PREFIX = "Fresh_Property";
        return factory.getOWLDataProperty(IRI.create(ontology.getOntologyID().getOntologyIRI().get() + "#"+ FRESH_Property_PREFIX));
    }
}
