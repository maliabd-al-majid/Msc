package Utlity;

import GraphLibs.Node;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

public record Pair(OWLNamedIndividual n1, OWLNamedIndividual n2) {
    @Override
    public String toString() {
        return "("+n1+","+n2+")";
    }
}
