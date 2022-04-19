package GraphLibs;

import org.semanticweb.owlapi.model.OWLPropertyAxiom;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
/**
 *
 * @author Mohamed Nadeem
 */
public record Edge(Node from, Node to, OWLPropertyExpression property) {

    @Override
    public String toString() {
        return property+"("+from.individual()+","+to.individual()+")";
    }


}