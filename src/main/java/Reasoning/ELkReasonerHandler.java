package Reasoning;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.github.jsonldjava.shaded.com.google.common.collect.Streams;
import OntologyLibs.FreshOWLEntityFactory.FreshOWLClassFactory;
import OntologyLibs.FreshOWLEntityFactory.FreshOWLNamedIndividualFactory;
import OntologyLibs.DisposableFinalVariable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.OWLAPIStreamUtils;
public class ELkReasonerHandler {
    private static final Logger logger = LogManager.getLogger(ELkReasonerHandler.class);
    private final DisposableFinalVariable<OWLOntology> ontology, ontologyCopy;
    private final OWLDataFactory factory;

    private final OWLReasoner reasoner;

    private final FreshOWLClassFactory freshOWLClassFactory;
    private final FreshOWLNamedIndividualFactory freshOWLNamedIndividualFactory;
    public ELkReasonerHandler(OWLOntology ontology) {
        this.ontology = new DisposableFinalVariable<>(ontology);
        try {
            this.ontologyCopy = new DisposableFinalVariable<>(ontology.getOWLOntologyManager().createOntology());
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e);
        }

        this.factory = ontology.getOWLOntologyManager().getOWLDataFactory();
        this.freshOWLClassFactory = FreshOWLClassFactory.of(ontologyCopy.get());
        this.freshOWLNamedIndividualFactory = FreshOWLNamedIndividualFactory.of(ontologyCopy.get());
        ontology.axioms(Imports.INCLUDED).forEach(axiom -> axiom.accept(addAxiomVisitor));
        ontology.getOWLOntologyManager().addOntologyChangeListener(changes -> {
            for (OWLOntologyChange change : changes) {
                if (change.isAddAxiom()) {
                    change.getAxiom().accept(addAxiomVisitor);
                } else if (change.isRemoveAxiom()) {
                    change.getAxiom().accept(removeAxiomVisitor);
                } else {
                    throw new UnsupportedOperationException("The reasoner facade does not support the change " + change);
                }
            }
        }, new SpecificOntologyChangeBroadcastStrategy(ontology));
        reasoner = new ElkReasonerFactory().createReasoner(ontologyCopy.get());

        long start = System.nanoTime();
        reasoner.precomputeInferences();
        reasoner.flush();
        logger.info("classification took "+(((double)System.nanoTime()-start)/1_000_000_000));
    }
    public void addExpressions(Iterable<OWLClassExpression> exps) {
        freshOWLClassFactory.addAdditionalKnownEntities(exps);
        for (OWLClassExpression owlClassExpression : exps) {
            if (!freshOWLClassFactory.containsObject(owlClassExpression)) {
                final OWLEquivalentClassesAxiom axiom = factory.getOWLEquivalentClassesAxiom(
                        owlClassExpression, freshOWLClassFactory.getEntity(owlClassExpression));
                ontologyCopy.get().addAxiom(axiom);
            }
        }
    }

    public void dispose() {
        reasoner.dispose();
//        alreadyAdded.clear();
        ontology.dispose();
        ontologyCopy.dispose();
    }
    @Deprecated
    public Set<OWLClassExpression> instanceOfExcludingOWLThing(OWLIndividual ind) {
        verifyKnows(ind);

        //timer.continueTimer();

        Set<OWLClassExpression> result = reasoner.types(freshOWLNamedIndividualFactory.getEntity(ind))
                .filter(c -> !c.isOWLThing())
                .map(name -> freshOWLClassFactory.getObject(name))
                .collect(Collectors.toSet());

        //timer.pause();

        return result;
    }
    public final Stream<OWLClassExpression> equivalentClasses(OWLClassExpression exp) throws IllegalArgumentException {
        verifyKnows(exp);
        return reasoner.equivalentClasses(freshOWLClassFactory.getEntity(exp))
                .map(name -> freshOWLClassFactory.getObject(name));
    }
    public final Set<OWLClassExpression> getEquivalentClasses(OWLClassExpression exp) throws IllegalArgumentException {
        return OWLAPIStreamUtils.asSet(equivalentClasses(exp));
    }
    @Deprecated
    public Set<OWLClassExpression> directSubsumersExcludingOWLThing(OWLClassExpression exp) throws IllegalArgumentException {
        verifyKnows(exp);

        return reasoner.superClasses(freshOWLClassFactory.getEntity(exp), true)
                .filter(c -> !c.isOWLThing())
                .map(name -> freshOWLClassFactory.getObject(name)).collect(Collectors.toSet());
    }
    @Deprecated
    public Set<OWLClassExpression> strictSubsumersExcludingOWLThing(OWLClassExpression exp) throws IllegalArgumentException {
        verifyKnows(exp);

        return reasoner.superClasses(freshOWLClassFactory.getEntity(exp), false)
                .filter(c -> !c.isOWLThing())
                .map(name -> freshOWLClassFactory.getObject(name))
                .collect(Collectors.toSet());
    }
    @Deprecated
    public Set<OWLClassExpression> equivalentIncludingOWLThingOrStrictlySubsumingExcludingOWLThing(OWLClassExpression exp) throws IllegalAccessError {
        verifyKnows(exp);

        Set<OWLClassExpression> result = strictSubsumersExcludingOWLThing(exp);
        result.addAll(getEquivalentClasses(exp));
        return result;
    }
    @Deprecated
    public Set<OWLClassExpression> directSubsumeesExcludingOWLNothing(OWLClassExpression exp) throws IllegalArgumentException {
        verifyKnows(exp);

        return reasoner.subClasses(freshOWLClassFactory.getEntity(exp), true)
                .filter(c -> !c.isOWLNothing())
                .map(name -> freshOWLClassFactory.getObject(name)).collect(Collectors.toSet());
    }
    @Deprecated
    public Set<OWLClassExpression> strictSubsumeesExcludingOWLThingAndOWLNothing(OWLClassExpression exp) throws IllegalArgumentException {
        verifyKnows(exp);
        Set<OWLClassExpression> result = reasoner.subClasses(freshOWLClassFactory.getEntity(exp), false)
                .filter(c -> (!c.isOWLThing() && !c.isOWLNothing()))
                .map(name -> freshOWLClassFactory.getObject(name))
                .collect(Collectors.toSet());

        // logger.info("Subsumees of "+exp+":");
        // result.stream().forEach(logger::info);

//        if(result.contains(null)){
//            System.out.println("Unexpected null caused by:");
//
//
//            reasoner.subClasses(freshOWLClassFactory.getEntity(exp), false)
//                    .filter(c -> !c.isOWLThing() && !c.isOWLNothing()).forEach(x -> {
//                        System.out.println(x);
//                        if(freshOWLClassFactory.getObject(x)==null)
//                            System.out.println(" -- that one I did not know.");
//                        if(!expressions.contains(x))
//                            System.out.println(" -- and indeed, it was not provided.");
//            });
//            System.exit(1);
//        }
        return result;
    }
    /**
     * Return or class expressions that are equivalent to exp, or subsumed by it. That is: the set of all subsummes,
     * including the equivalent ones.
     */
    @Deprecated
    public Set<OWLClassExpression> equivalentIncludingOWLThingAndOWLNothingOrStrictlySubsumedByExcludingOWLThingAndOWLNothing(OWLClassExpression exp) throws IllegalArgumentException {
        verifyKnows(exp);

        Set<OWLClassExpression> result = strictSubsumeesExcludingOWLThingAndOWLNothing(exp);
        result.addAll(getEquivalentClasses(exp));
        return result;
    }

    private void verifyKnows(OWLClassExpression exp) throws IllegalArgumentException {
        if(!freshOWLClassFactory.containsObject(exp))
            throw new IllegalArgumentException("ClassExpression unknown: "+exp);
    }


    private void verifyKnows(OWLIndividual ind) throws IllegalArgumentException {
        if (!freshOWLNamedIndividualFactory.containsObject(ind))
            throw new IllegalArgumentException("Individual unknown: " + ind);
    }
    /**
     * Checks whether exp is subsumed by some element in the set.
     */

    private final OWLAxiomVisitor addAxiomVisitor = new OWLAxiomVisitor() {

        @Override
        public void visit(OWLDeclarationAxiom axiom) {
            ontologyCopy.get().add(axiom);
            freshOWLNamedIndividualFactory.addAdditionalKnownEntity(axiom.getEntity());
            freshOWLClassFactory.addAdditionalKnownEntity(axiom.getEntity());
        }

        @Override
        public void visit(OWLClassAssertionAxiom axiom) {
            if (!axiom.getClassExpression().isOWLClass()) {
                doDefault(axiom);
            } else if (axiom.getIndividual().isAnonymous()) {
                ontologyCopy.get().addAxiom(factory.getOWLClassAssertionAxiom(
                        axiom.getClassExpression(),
                        freshOWLNamedIndividualFactory.getEntity(axiom.getIndividual())));
            } else {
                ontologyCopy.get().add(axiom);
                freshOWLNamedIndividualFactory.addAdditionalKnownEntity(axiom.getIndividual());
            }
        }

        @Override
        public void visit(OWLObjectPropertyAssertionAxiom axiom) {
            if (axiom.getSubject().isAnonymous() || axiom.getObject().isAnonymous()) {
                ontologyCopy.get().addAxiom(factory.getOWLObjectPropertyAssertionAxiom(
                        axiom.getProperty(),
                        freshOWLNamedIndividualFactory.getEntity(axiom.getSubject()),
                        freshOWLNamedIndividualFactory.getEntity(axiom.getObject())));
            } else {
                ontologyCopy.get().add(axiom);
            }
            if (axiom.getSubject().isOWLNamedIndividual())
                freshOWLNamedIndividualFactory.addAdditionalKnownEntity(axiom.getSubject());
            if (axiom.getObject().isOWLNamedIndividual())
                freshOWLNamedIndividualFactory.addAdditionalKnownEntity(axiom.getObject());
        }

        @Override
        public void visit(OWLSubClassOfAxiom axiom) {
            ontologyCopy.get().add(axiom);
            addExpressions(axiom.getNestedClassExpressions());
        }

        @Override
        public void visit(OWLEquivalentClassesAxiom axiom) {
            ontologyCopy.get().add(axiom);
            addExpressions(axiom.getNestedClassExpressions());
        }

        @Override
        public void doDefault(Object object) {
            throw new IllegalArgumentException("The reasoner facade does not support the axiom " + object + ".");
        }

    };

    private final OWLAxiomVisitor removeAxiomVisitor = new OWLAxiomVisitor() {

        @Override
        public void visit(OWLDeclarationAxiom axiom) {
            ontologyCopy.get().remove(axiom);
        }

        @Override
        public void visit(OWLClassAssertionAxiom axiom) {
            if (!axiom.getClassExpression().isOWLClass()) {
                doDefault(axiom);
            } else if (axiom.getIndividual().isAnonymous()) {
                ontologyCopy.get().removeAxiom(factory.getOWLClassAssertionAxiom(
                        axiom.getClassExpression(),
                        freshOWLNamedIndividualFactory.getEntity(axiom.getIndividual())));
            } else {
                ontologyCopy.get().remove(axiom);
            }
        }

        @Override
        public void visit(OWLObjectPropertyAssertionAxiom axiom) {
            if (axiom.getSubject().isAnonymous() || axiom.getObject().isAnonymous()) {
                ontologyCopy.get().removeAxiom(factory.getOWLObjectPropertyAssertionAxiom(
                        axiom.getProperty(),
                        freshOWLNamedIndividualFactory.getEntity(axiom.getSubject()),
                        freshOWLNamedIndividualFactory.getEntity(axiom.getObject())));
            } else {
                ontologyCopy.get().remove(axiom);
            }
        }

        @Override
        public void visit(OWLSubClassOfAxiom axiom) {
            ontologyCopy.get().remove(axiom);
        }

        @Override
        public void visit(OWLEquivalentClassesAxiom axiom) {
            ontologyCopy.get().remove(axiom);
        }

        @Override
        public void doDefault(Object object) {
            throw new IllegalArgumentException("The reasoner facade does not support the axiom " + object + ".");
        }

    };

}
