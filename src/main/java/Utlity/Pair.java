package Utlity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
/**
 *
 * @author Mohamed Nadeem
 */
public record Pair(OWLNamedIndividual n1, OWLNamedIndividual n2) {
    @Override
    public String toString() {
        return "("+n1+","+n2+")";
    }
}
