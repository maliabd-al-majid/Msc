package cmd;

import MscConstruction.MscBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
            MscBuilder mscBuilder= new MscBuilder(ontology,individualList.get(0));
            boolean mscFound=mscBuilder.buildMsc();

            System.out.print("- Decision: ");
            if(mscFound)
                System.out.println("Msc found");
            else
                System.out.println("No Msc found");
            System.out.println("-----------------------------------------");
            print(mscBuilder.getCanonicalGraphConstructed());
        }
        else throw new IOException("Missing Parameters");


    }
}
