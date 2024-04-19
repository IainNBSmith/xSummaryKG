// Adapted from code samples from Standford NLP toolkit
// XTE settings are from the XTE packages usage
import com.sun.xml.bind.api.impl.NameConverter;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.ie.util.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.semgraph.*;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.util.CoreMap;




import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.Mention;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.util.Collection;
import java.util.Properties;

import java.util.*;
import java.lang.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.io.*;

import core.Entailment;
import edu.uniba.di.lacam.kdde.ws4j.RelatednessCalculator;


public class Main {

    // XTE settings
    private static String configFile = "../../NLP/XTE-master/config.txt"; //the configuration file path
    private static String kb = "WN"; //WN (WordNet), WKT (Wikitionary), WKP (Wikipedia) or WBT (Webster's)
    private static Entailment entail = new Entailment(kb, configFile);

    //Train the TED module
    private static void train (String dataset){

        entail.train(dataset);
    }

    //Test a single T-H pair
    private static void testPair (String text, String hypothesis){

        String result = entail.processPair(text, hypothesis, kb);
        System.out.println(result);
    }

    //Test a dataset
    private static void testDataset(String dataset, String outputFile){

        entail.processDataset(dataset, outputFile, kb);
    }


    public static Boolean checkTripleValid(RelationTriple triple) {
        // check that subject and object are both <= 10 characters
        int nword = 10;
        return (triple.subjectLemmaGloss().split("\\s").length <= nword &&
                triple.objectLemmaGloss().split("\\s").length <= nword);
    }

    public static Boolean tripleMatcher(RelationTriple t1, RelationTriple t2) {
        // check if the triples contain each other, in one direction
        //return (new String(t1.subjectLemmaGloss()).contains(t2.subjectLemmaGloss()) &&
        //        new String(t1.relationLemmaGloss()).contains(t2.relationLemmaGloss()) &&
        //        new String(t1.objectLemmaGloss()).contains(t2.objectLemmaGloss()));
        String check = entail.processPair(t1.subjectLemmaGloss() + " " + t1.relationLemmaGloss() + " " + t1.objectLemmaGloss(),
                t2.subjectLemmaGloss() + " " + t2.relationLemmaGloss() + " " + t2.objectLemmaGloss(),
                kb);

        //return (new String(t1.relationLemmaGloss()).contains(t2.relationLemmaGloss()) ||
        //        new String(t2.relationLemmaGloss()).contains(t1.relationLemmaGloss()));
        return check.equals("yes") && (subjectMatcher(t1, t2) || objectMatcher(t1, t2));
    }
    public static  Boolean exactTripleMatcher(RelationTriple t1, RelationTriple t2) {
        return subjectMatcher(t1, t2) &&
                (new String(t1.relationLemmaGloss()).contains(t2.relationLemmaGloss()) ||
                new String(t2.relationLemmaGloss()).contains(t1.relationLemmaGloss())) &&
                        objectMatcher(t1, t2);
    }

    public static Boolean subjectMatcher(RelationTriple t1, RelationTriple t2) {
        return (new String(t1.subjectLemmaGloss()).contains(t2.subjectLemmaGloss()) ||
                new String(t2.subjectLemmaGloss()).contains(t1.subjectLemmaGloss()) ||
                new String(t1.subjectLemmaGloss()).contains(t2.objectLemmaGloss()) ||
                new String(t2.objectLemmaGloss()).contains(t1.subjectLemmaGloss()));
    }

    public static Boolean objectMatcher(RelationTriple t1, RelationTriple t2) {
        return (new String(t1.objectLemmaGloss()).contains(t2.objectLemmaGloss()) ||
                new String(t2.objectLemmaGloss()).contains(t1.objectLemmaGloss()) ||
                new String(t1.objectLemmaGloss()).contains(t2.subjectLemmaGloss()) ||
                new String(t2.subjectLemmaGloss()).contains(t1.objectLemmaGloss()));
    }

    public static Boolean subjectMatch(RelationTriple t1, RelationTriple t2) {
        // use other methods which is why distinct
        return (new String(t1.subjectLemmaGloss()).contains(t2.subjectLemmaGloss()) ||
                new String(t2.subjectLemmaGloss()).contains(t1.subjectLemmaGloss()));
    }
    public static Boolean relationMatch(RelationTriple t1, RelationTriple t2) {
        // use other methods which is why distinct
        String check = entail.processPair(t1.subjectLemmaGloss() + " " + t1.relationLemmaGloss() + " " + t1.objectLemmaGloss(),
                t2.subjectLemmaGloss() + " " + t2.relationLemmaGloss() + " " + t2.objectLemmaGloss(),
                kb);

        //return (new String(t1.relationLemmaGloss()).contains(t2.relationLemmaGloss()) ||
        //        new String(t2.relationLemmaGloss()).contains(t1.relationLemmaGloss()));
        return check.equals("yes");
    }

    public static Boolean subjectRelationMatcher(RelationTriple t1, RelationTriple t2) {
        // check if only the subject and relation match (different object)
        return (subjectMatch(t1, t2) && relationMatch(t1, t2));
    }

    public static void compareTripleSets(Collection<RelationTriple> tripInit, Collection<RelationTriple> tripSum) {
        // so far so good: need to see how we compare odd strings and swapped subject object pairs
        // compare directly if triples are matched, then print those that are not
        /*
        // check if the triples are there
        for (RelationTriple triple : tripInit) {
            System.out.println(triple.confidence + "\t" +
                    triple.subjectLemmaGloss() + "\t" +
                    triple.relationLemmaGloss() + "\t" +
                    triple.objectLemmaGloss());
        }

        for (RelationTriple triple : tripSum) {
            System.out.println(triple.confidence + "\t" +
                    triple.subjectLemmaGloss() + "\t" +
                    triple.relationLemmaGloss() + "\t" +
                    triple.objectLemmaGloss());
        }
         */
        //RelatednessCalculator
        String trainDataset = "../../NLP/XTE-master/Datasets/RTE+SICK_train_set.txt"; //the training dataset (in text format)

        //train(trainDataset); // do not need to train from what I can tell after at least first
        int i = -1;
        int j = -1;
        ArrayList<Boolean> matchedInit = new ArrayList<Boolean>();
        ArrayList<Boolean> matchedSum = new ArrayList<Boolean>();
        ArrayList<Boolean> subjectInit = new ArrayList<Boolean>();
        ArrayList<Boolean> subjectSum = new ArrayList<Boolean>();
        ArrayList<Boolean> objectInit = new ArrayList<Boolean>();
        ArrayList<Boolean> objectSum = new ArrayList<Boolean>();

        for (int ii = 0; ii < tripInit.size(); ii++){
            matchedInit.add(false);
            subjectInit.add(false);
            objectInit.add(false);
        }
        for (int ii = 0; ii < tripSum.size(); ii++){
            matchedSum.add(false);
            subjectSum.add(false);
            objectSum.add(false);
        }

        for (RelationTriple tI : tripInit) {
            i += 1;
            j = -1;
            for (RelationTriple tS : tripSum) {
                j += 1;
                // before the Entailment check compare the subjects and objects
                // also need to see what happens where it does not do the word comparisons...
                //System.out.println("\t" + tI.subjectLemmaGloss() + " // " + tS.subjectLemmaGloss());
                //System.out.println("\t" + tI.relationLemmaGloss() + " // " + tS.relationLemmaGloss());
                //System.out.println("\t" + tI.objectLemmaGloss() + " // " + tS.objectLemmaGloss());
                //testPair(tI.relationLemmaGloss(), tS.relationLemmaGloss());
                // a simple test
                //System.out.println(entail.processPair(tI.subjectLemmaGloss() + " " + tI.relationLemmaGloss() + " " + tI.objectLemmaGloss(),
                        //tS.subjectLemmaGloss() + " " + tS.relationLemmaGloss() + " " + tS.objectLemmaGloss(),
                        //kb));
                if (tripleMatcher(tI, tS)) {
                    matchedInit.set(i, true);
                    matchedSum.set(j, true);
                    //System.out.println("matched");
                    //System.out.println("\t" + tI.subjectLemmaGloss() + " // " + tS.subjectLemmaGloss());
                    //System.out.println("\t" + tI.relationLemmaGloss() + " // " + tS.relationLemmaGloss());
                    //System.out.println("\t" + tI.objectLemmaGloss() + " // " + tS.objectLemmaGloss());
                }
                if (subjectMatcher(tI, tS)) subjectInit.set(i, true);
                if (objectMatcher(tI, tS)) objectInit.set(i, true);
                if (subjectMatcher(tS, tI)) subjectSum.set(j, true);
                if (objectMatcher(tS, tI)) objectSum.set(j, true);
            }
        }

        for (RelationTriple tI : tripInit) {
            //if(!subjectInit.get(i)) System.out.println("Removed Entity: " + tI.subjectLemmaGloss());
            //if(!objectInit.get(i)) System.out.println("Removed Entity: " + tI.objectLemmaGloss());
            System.out.println("Original Relation: " + tI.subjectLemmaGloss() + " -- " +
                    tI.relationLemmaGloss() + " --> " + tI.objectLemmaGloss());
            //if(matchedInit.get(i) && relRatioGreat) intersection += 1;
        }
        for (RelationTriple tS : tripSum) {
            //if(!subjectInit.get(i)) System.out.println("Removed Entity: " + tI.subjectLemmaGloss());
            //if(!objectInit.get(i)) System.out.println("Removed Entity: " + tI.objectLemmaGloss());
            System.out.println("Summary Relation: " + tS.subjectLemmaGloss() + " -- " +
                    tS.relationLemmaGloss() + " --> " + tS.objectLemmaGloss());
            //if(matchedInit.get(i) && relRatioGreat) intersection += 1;
        }

        // output differences
        i = 0;
        Boolean relRatioGreat = matchedInit.size()/((double)matchedSum.size()) < 1.0;
        Double intersection = 0.0;
        for (RelationTriple tI : tripInit) {
            //if(!subjectInit.get(i)) System.out.println("Removed Entity: " + tI.subjectLemmaGloss());
            //if(!objectInit.get(i)) System.out.println("Removed Entity: " + tI.objectLemmaGloss());
            if(!matchedInit.get(i)) System.out.println("Removed Relation: " + tI.subjectLemmaGloss() + " -- " +
                    tI.relationLemmaGloss() + " --> " + tI.objectLemmaGloss());
            if(matchedInit.get(i) && relRatioGreat) intersection += 1;
            i += 1;
        }

        j = 0;
        for (RelationTriple tS : tripSum) {
            //if(!subjectSum.get(j)) System.out.println("Added Entity: " + tS.subjectLemmaGloss());
            //if(!objectSum.get(j)) System.out.println("Added Entity: " + tS.objectLemmaGloss());
            if(!matchedSum.get(j)) System.out.println("Added Relation: " + tS.subjectLemmaGloss() + " -- " +
                    tS.relationLemmaGloss() + " --> " + tS.objectLemmaGloss());
            //System.out.println(tS.subjectLemmaGloss() + " -- " +
            //        tS.relationLemmaGloss() + " --> " + tS.objectLemmaGloss());
            if(matchedSum.get(j) && !relRatioGreat) intersection += 1;
            j += 1;
        }

        System.out.println("Precision = " + intersection/matchedSum.size());
        System.out.println("Recall = " + intersection/matchedInit.size());
        System.out.println("Relation Ratio = " + matchedInit.size()/((double)matchedSum.size()));


    }

    public static Collection<RelationTriple> extractRelations(String docName) {
        // get all the relations in a valid format in one collection
        String content = "default"; // working so far
        // requires exception handling for opening file
        try {
            content = new String(Files.readString(Paths.get("../Data/" + docName), Charset.defaultCharset()));
            System.out.println(content);
        }
        catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        // set up pipeline properties
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,depparse,natlog,coref,openie");
        props.setProperty("ner.rulesOnly", "true"); // only use rules no statistics
        props.setProperty("coref.algorithm", "statistical"); // only this or neural and this is a bit better
        props.setProperty("openie.resolve_coref", "true"); // coref requirement is missing in error message
        // props.setProperty("openie.triple.strict", "true"); //NEW see if performance of found relationships improves

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        // create a document object
        Annotation document = new Annotation(content);
        // annnotate the document
        pipeline.annotate(document);

        Collection<RelationTriple> finalTriples = new ArrayList<RelationTriple>();
        for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
            // Get the OpenIE triples for the sentence
            Collection<RelationTriple> triples =
                    sentence.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class);
            // Print the triples
            for (RelationTriple triple : triples) {
                Boolean same = false;
                Boolean trimmed = false;
                if (checkTripleValid(triple)) {
                    for (RelationTriple otriple : triples) {
                        if (same) {
                            break;
                        }
                        if (checkTripleValid(otriple) && otriple != triple) {
                            // check that the triple has exact matches in one way
                            // keep the larger triple (more info)
                            if (exactTripleMatcher(triple, otriple) &&
                                    ( ( triple.subjectLemmaGloss().length() +
                                            triple.objectLemmaGloss().length() +
                                            triple.relationLemmaGloss().length() ) < (otriple.subjectLemmaGloss().length() +
                                            otriple.objectLemmaGloss().length() +
                                            otriple.relationLemmaGloss().length() ) )
                            ) {
                                same = true;
                            }
                        }
                    }
                } else {
                    //remove current relation
                    trimmed = true;
                }
                if (!trimmed && !same) {
                    // put it into final file
                    finalTriples.add(triple);
                }
            }
        }
        return finalTriples;
    }

    public static void main(String[] args) {
        // articles in Data folder, change names for different comparisons
        Collection<RelationTriple> triplesInit = extractRelations("trumpUnproblematic.txt");
        Collection<RelationTriple> triplesSum = extractRelations("trumpUnproblematic-copilot-noprompt.txt");

        compareTripleSets(triplesInit, triplesSum);
    }
}