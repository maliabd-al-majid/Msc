package GraphLibs;

import org.semanticweb.owlapi.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
/**
 *
 * @author Mohamed Nadeem
 */
public class FreshGraphEntity {
    private final Map<OWLClassExpression, OWLNamedIndividual> class2ind;
   // private int fresh_individual_index=0;
    private final OWLOntology ontology;
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
            OWLNamedIndividual owlNamedIndividual = factory.getOWLNamedIndividual(IRI.create(ontology.getOntologyID().getOntologyIRI().get() + "#"+ expression));
            class2ind.putIfAbsent(expression, owlNamedIndividual);
            return owlNamedIndividual;
        }else return class2ind.get(expression);
    }
    public boolean individualExists(OWLClassExpression expression){
        return class2ind.containsKey(expression);
    }

}
