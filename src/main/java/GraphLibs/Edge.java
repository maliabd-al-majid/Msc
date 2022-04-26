package GraphLibs;

import org.semanticweb.owlapi.model.OWLPropertyAxiom;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
/**
 *
 * @author Mohamed Nadeem
 */
public record Edge(int from, int to, OWLPropertyExpression property) {

    @Override
    public String toString() {
        return property+"(v"+from+",v"+to+")";
    }


}