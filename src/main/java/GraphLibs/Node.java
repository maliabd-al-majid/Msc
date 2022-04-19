package GraphLibs;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import java.util.Set;
/**
 *
 * @author Mohamed Nadeem
 */
public record Node(int label, OWLNamedIndividual individual, Set<OWLClassExpression> concept) {


    @Override
    public String toString() {
        return "v" + label;
    }
}