package quotes.utils;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.sentiment.*;

import java.util.Properties;

public class SentimentAnalyzer {
    public static void printSentiment(String text) {
        // Create a new CoreNLP pipeline with the sentiment annotator
        Properties props = new Properties();
        props.setProperty("annotators",
                "tokenize, ssplit, parse, sentiment");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // Analyze the sentiment of a sample text
        CoreDocument doc = new CoreDocument(text);
        pipeline.annotate(doc);
        var sentimentScore = doc.sentences().get(0).sentiment();

        System.out.println("sentiment = " + sentimentScore);
    }
}

