package cmd;

import MscConstruction.MscBuilderFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static Utlity.GraphUtility.print;

/**
 *
 * @author Mohamed Nadeem
 */
public class ComputeMSC {
    private static final Logger logger = LogManager.getLogger(ComputeMSC.class);
    public static void main(String[] args) throws OWLOntologyCreationException, IOException {

        if (args.length>1) {
            OWLOntology ontology =
                    OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new File(args[0]));
            List<OWLNamedIndividual> individualList=new ArrayList<>();
            for (int i = 1; i < args.length; i++) {
                OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();
                OWLNamedIndividual individual = null;
                if (ontology.getOntologyID().getOntologyIRI().isPresent())
                    individual = factory.getOWLNamedIndividual(IRI.create(ontology.getOntologyID().getOntologyIRI().get() + "#" + args[i].toLowerCase()));
                else throw new IOException("Ontology IRI not found");
                if(ontology.getIndividualsInSignature().contains(individual))
                    individualList.add(individual);

                else
                    logger.error("Individual: " +individual + " not found in Ontology");
            }
            logger.debug("- Processing Individuals : " +individualList);
            System.out.println("Processing Indvidual:"+individualList.get(0));
            MscBuilderFactory mscBuilderFactory = new MscBuilderFactory(ontology,individualList.get(0));
            boolean mscFound= mscBuilderFactory.buildMsc();

            System.out.print("- Decision: ");
            if(mscFound)
                System.out.println("Msc found");
            else
                System.out.println("No Msc found");
            System.out.println("-----------------------------------------");
            print(mscBuilderFactory.getGraphConstructed());
        }

        else if(args.length>0){
            long start = System.currentTimeMillis();
            OWLOntology ontology =
                    OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new File(args[0]));
            List<OWLNamedIndividual> individualSet =ontology.getIndividualsInSignature().stream().toList();
            int ind=0;
            for(int i=0;i<individualSet.size();i++)
                if(!ontology.getObjectPropertyAssertionAxioms(individualSet.get(i)).isEmpty()) {
                    //System.out.println(ontology.getObjectPropertyAssertionAxioms(individualSet.get(i)));
                    if (ontology.getObjectPropertyAssertionAxioms(individualSet.get(i)).size() > ontology.getObjectPropertyAssertionAxioms(individualSet.get(ind)).size()) {
                        ind = i;
                    }
                }

            //System.out.println(individualSet);
            //int index= new Random().nextInt(individualSet.size());
            // Constructing a random individual from the ontology.
            System.out.print("- Individual: ");
            System.out.println(individualSet.get(ind));
            // Computing the Msc if exists.
            MscBuilderFactory mscBuilderFactory = new MscBuilderFactory(ontology,individualSet.get(ind));

            boolean mscFound= mscBuilderFactory.buildMsc();

            System.out.print("- Decision: ");
            if(mscFound)
                System.out.println("Msc");
            else
                System.out.println("No Msc");
            System.out.println("-----------------------------------------");
            //print(mscBuilderFactory.getCanonicalModel());
            mscBuilderFactory.getCanonicalModel().printAllPaths(mscBuilderFactory.getCanonicalModel().getRoot());
            //System.out.println(mscBuilderFactory.getCanonicalModel().getPaths(mscBuilderFactory.getCanonicalModel().getRoot()));
            mscBuilderFactory.getGraphConstructed().printAllPaths(mscBuilderFactory.getGraphConstructed().getRoot());
            int previousBound = (individualSet.size()*individualSet.size()) +mscBuilderFactory.getCanonicalModel().getGraphDepth() +1;
            int newBound = mscBuilderFactory.getGraphConstructed().getGraphDepth();
            System.out.println("- Previous Bound: "+ previousBound);
            System.out.println("- New Bound: "+ newBound);
            System.out.println("-----------------------------------------");
            System.out.print("- ExecutionTime: ");
            long end = System.currentTimeMillis();

            NumberFormat formatter = new DecimalFormat("#0.00000");
            System.out.print(formatter.format((end - start) / 1000d) + " seconds");
        }
        else throw new IOException("Missing Parameters");


    }
}
