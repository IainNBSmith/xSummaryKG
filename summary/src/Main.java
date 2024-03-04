// Code sample from Standford NLP toolkit
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


public class Main {


    public static Boolean checkTripleValid(RelationTriple triple) {
        // check that subject and object are both <= 10 characters
        int nword = 10;
        return (triple.subjectLemmaGloss().split("\\s").length <= nword &&
                triple.objectLemmaGloss().split("\\s").length <= nword);
    }

    public static Boolean tripleMatcher(RelationTriple t1, RelationTriple t2) {
        // check if the triples contain each other, in one direction
        return (new String(t1.subjectLemmaGloss()).contains(t2.subjectLemmaGloss()) &&
                new String(t1.relationLemmaGloss()).contains(t2.relationLemmaGloss()) &&
                new String(t1.objectLemmaGloss()).contains(t2.objectLemmaGloss()));
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
        for (RelationTriple tI : tripInit) {
            for (RelationTriple tS : tripSum) {
                if (tripleMatcher(tI, tS) || tripleMatcher(tS, tI)) {
                    System.out.println("matched");
                    System.out.println("\t" + tI.subjectLemmaGloss() + " // " + tS.subjectLemmaGloss());
                    System.out.println("\t" + tI.relationLemmaGloss() + " // " + tS.relationLemmaGloss());
                    System.out.println("\t" + tI.objectLemmaGloss() + " // " + tS.objectLemmaGloss());
                }
            }
        }

        for (RelationTriple triple : tripInit) {
            System.out.println("Original" + "\t" +
                    triple.subjectLemmaGloss() + "\t" +
                    triple.relationLemmaGloss() + "\t" +
                    triple.objectLemmaGloss());
        }

        for (RelationTriple triple : tripSum) {
            System.out.println("Summary" + "\t" +
                    triple.subjectLemmaGloss() + "\t" +
                    triple.relationLemmaGloss() + "\t" +
                    triple.objectLemmaGloss());
        }

    }

    public static Collection<RelationTriple> extractRelations(String docName) {
        // get all the relations in a valid format in one collection
        String content = "default"; // working so far
        // requires exception handling here
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
        props.setProperty("annotators", "tokenize,pos,lemma,ner,depparse,natlog,coref,openie");
        props.setProperty("ner.rulesOnly", "true"); // only use rules no statistics
        props.setProperty("coref.algorithm", "statistical"); // only this or neural and this is a bit better
        props.setProperty("openie.resolve_coref", "true"); // coref requirement is missing in error message

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        // create a document object
        // CoreDocument document = new CoreDocument(content);
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
                            if (new String(otriple.subjectLemmaGloss()).contains(triple.subjectLemmaGloss()) &&
                                    new String(otriple.relationLemmaGloss()).contains(triple.relationLemmaGloss()) &&
                                    new String(otriple.objectLemmaGloss()).contains(triple.objectLemmaGloss())) {
                                same = true;
                            }
                        }
                    }
                } else {
                    //System.out.println("trimmed");
                    trimmed = true;
                }
                //if (same) {
                //    System.out.println("matched");
                //}
                if (!trimmed && !same) {
                    // put it into final file, for now just count
                    finalTriples.add(triple);
                }
            }
        }
        return finalTriples;
    }

    //public static String text = "Joe Smith was born in California. In 2017, he went to Paris, France in the summer. His flight left at 3:00pm on July 10th, 2017. After eating some escargot for the first time, Joe said, \"That was delicious!\" He sent a postcard to his sister Jane Smith. After hearing about Joe's trip, Jane decided she might go to France one day.";
    // add just extracting entity relations
    public static void main(String[] args) {
        Collection<RelationTriple> triplesInit = extractRelations("dailyshow.txt");
        Collection<RelationTriple> triplesSum = extractRelations("dailyshow-gpt.txt");

        compareTripleSets(triplesInit, triplesSum);
    }
}

        /*
        String content = "default"; // working so far
        // requires exception handling here
        try {
            content = new String(Files.readString(Paths.get("../Data/dailyshow.txt"), Charset.defaultCharset()));
            System.out.println(content);
        }
        catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        // set up pipeline properties
        Properties props = new Properties();
        // set the list of annotators to run
        // coref for correference annotation
        // go through triples and resolve corefferences
        // wonder how I should have this done
        props.setProperty("annotators", "tokenize,pos,lemma,ner,depparse,natlog,coref,openie");
        // set a property for an annotator, in this case the coref annotator is being set to use the neural algorithm
        props.setProperty("ner.rulesOnly", "true"); // only use rules no statistics
        props.setProperty("coref.algorithm", "statistical"); // only this or neural and this is a bit better
        props.setProperty("openie.resolve_coref", "true"); // coref requirement is missing in error message
        // build pipeline
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        // create a document object
        // CoreDocument document = new CoreDocument(content);
        Annotation document = new Annotation(content);
        // annnotate the document
        pipeline.annotate(document);

        /*
        System.out.println("---");
        System.out.println("coref chains");
        for (CorefChain cc : document.get(CorefCoreAnnotations.CorefChainAnnotation.class).values()) {
            System.out.println("\t" + cc);
        }
        for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
            System.out.println("---");
            System.out.println("mentions");
            for (Mention m : sentence.get(CorefCoreAnnotations.CorefMentionsAnnotation.class)) {
                System.out.println("\t" + m);
            }
        }
        */
        /*
        // triples
        // need to do named entity recognition and disambiguation
        int ncount = 0;
        for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
            // Get the OpenIE triples for the sentence
            Collection<RelationTriple> triples =
                    sentence.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class);
            // Print the triples
            for (RelationTriple triple : triples) {
                System.out.println(triple.confidence + "\t" +
                        triple.subjectLemmaGloss() + "\t" +
                        triple.relationLemmaGloss() + "\t" +
                        triple.objectLemmaGloss());
                Boolean same = false;
                Boolean trimmed = false;
                if (checkTripleValid(triple)) {
                    for (RelationTriple otriple : triples) {
                        if (same) {
                            break;
                        }
                        if (checkTripleValid(otriple) && otriple != triple) {
                            if (new String(otriple.subjectLemmaGloss()).contains(triple.subjectLemmaGloss()) &&
                                    new String(otriple.relationLemmaGloss()).contains(triple.relationLemmaGloss()) &&
                                    new String(otriple.objectLemmaGloss()).contains(triple.objectLemmaGloss())) {
                                same = true;
                            }
                        }
                    }
                } else {
                    System.out.println("trimmed");
                    trimmed = true;
                }
                if (same) {
                    System.out.println("matched");
                }
                if (!trimmed && !same) {
                 // put it into final file, for now just count
                 ncount += 1;
                }
                }
            }
        System.out.println(ncount);
        }
        */
        //Map<Integer, CorefChain> corefChains = document.CorefChains();
        //System.out.println("Example: coref chains for document");
        //System.out.println(corefChains);
        //System.out.println();

        // examples
/*
        // 10th token of the document
        CoreLabel token = document.tokens().get(3);
        System.out.println("Example: token");
        System.out.println(token);
        System.out.println();

        // text of the first sentence
        String sentenceText = document.sentences().get(0).text();
        System.out.println("Example: sentence");
        System.out.println(sentenceText);
        System.out.println();

        // second sentence
        CoreSentence sentence = document.sentences().get(1);

        // list of the part-of-speech tags for the second sentence
        List<java.lang.String> posTags = sentence.posTags();
        System.out.println("Example: pos tags");
        System.out.println(posTags);
        System.out.println();

        // list of the ner tags for the second sentence
        List<String> nerTags = sentence.nerTags();
        System.out.println("Example: ner tags");
        System.out.println(nerTags);
        System.out.println();

        // constituency parse for the second sentence
        Tree constituencyParse = sentence.constituencyParse();
        System.out.println("Example: constituency parse");
        System.out.println(constituencyParse);
        System.out.println();

        // dependency parse for the second sentence
        SemanticGraph dependencyParse = sentence.dependencyParse();
        System.out.println("Example: dependency parse");
        System.out.println(dependencyParse);
        System.out.println();

        /*
        // kbp relations found in fifth sentence
        List<RelationTriple> relations =
                document.sentences().get(11).relations();
        System.out.println("Example: relation");
        System.out.println(relations.get(0));
        System.out.println(relations.get(1));
        System.out.println();

         */
/*
        // entity mentions in the second sentence
        List<CoreEntityMention> entityMentions = sentence.entityMentions();
        System.out.println("Example: entity mentions");
        System.out.println(entityMentions);
        System.out.println();

        // coreference between entity mentions
        CoreEntityMention originalEntityMention = document.sentences().get(0).entityMentions().get(1);
        System.out.println("Example: original entity mention");
        System.out.println(originalEntityMention);
        System.out.println("Example: canonical entity mention");
        System.out.println(originalEntityMention.canonicalEntityMention().get());
        System.out.println();

        // get document wide coref info
        // coreference resolution
        Map<Integer, CorefChain> corefChains = document.corefChains();
        System.out.println("Example: coref chains for document");
        System.out.println(corefChains);
        System.out.println();

        // get quotes in document
        List<CoreQuote> quotes = document.quotes();
        CoreQuote quote = quotes.get(0);
        System.out.println("Example: quote");
        System.out.println(quote);
        System.out.println();

        // original speaker of quote
        // note that quote.speaker() returns an Optional
        System.out.println("Example: original speaker of quote");
        System.out.println(quote.speaker().get());
        System.out.println();

        // canonical speaker of quote
        System.out.println("Example: canonical speaker of quote");
        System.out.println(quote.canonicalSpeaker().get());
        System.out.println();
*/
