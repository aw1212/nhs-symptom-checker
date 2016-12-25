package nhs.uk.co.alessandra.indexing;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.surround.parser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanNotQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.google.common.collect.ImmutableSet;

import nhs.uk.co.alessandra.indexing.factory.SiteIndexerFactory;
import nhs.uk.co.alessandra.indexing.lucene.MatchingDocumentSearcher;
import nhs.uk.co.alessandra.indexing.repository.NHSSymptomIndexerRepository;
import nhs.uk.co.alessandra.indexing.symptom.Symptom;

@SpringBootApplication
public class NHSSymptomIndexer implements CommandLineRunner {

    @Autowired
    private SiteIndexerFactory siteIndexerFactory;
    @Autowired
    private MatchingDocumentSearcher searcher;
    @Autowired
    private NHSSymptomIndexerRepository nhsSymptomIndexerRepository;
    @Autowired
    private AccuracyCalculator accuracyCalculator;

    public static void main(final String[] args) throws SQLException, IOException, ParseException, QueryNodeException {
        SpringApplication.run(NHSSymptomIndexer.class, args);
    }

    @Override
    public void run(String... args) throws IOException {
        // crawl nhs sites
        // save symptoms that match SymptomList in H2 database
        // save all content from urls as documents in Lucene index
        siteIndexerFactory.startSiteIndexer();
        System.out.println("");

        // print symptoms that were extracted from indexer and saved in H2 database
        printSymptomsFromDatabase();

        // print percentage accuracy of symptoms extracted from indexer and saved in H2
        Set<Ailment> ailments = ImmutableSet.of(Ailment.BRONCHITIS, Ailment.CORONARY_HEART_DISEASE,
                Ailment.FROZEN_SHOULDER, Ailment.HEART_BLOCK);
        for (Ailment ailment : ailments) {
            long accuracy = accuracyCalculator.getAccuracy(ailment);
            System.out.println(String.format("Accuracy for %s: %s ", ailment.getName(), accuracy));
        }

        // get documents (via a search query) that were added by indexer into lucene index
        printDocumentsFromIndex();
        System.out.println("");
    }

    private void printSymptomsFromDatabase() {
        for (Symptom symptom : nhsSymptomIndexerRepository.getSymptoms()) {
            System.out.println(
                    " AILMENT: " + symptom.getAilment() +
                    " SYMPTOM: " + symptom.getSymptom());
        }
        System.out.println("");
    }

    private void printDocumentsFromIndex() throws IOException {
        // query string search examples
        try {
            // example of regular term query search
            searcher.searchByQueryString("nausea");
            // example of boolean search with proximity search
            searcher.searchByQueryString("nausea NOT \"side effects nausea\"~15");
        } catch (QueryNodeException e) {
            System.out.println("Problem processing search string");
        } catch (ParseException | org.apache.lucene.queryparser.classic.ParseException e) {
            String errorMessage = String.format("Problem parsing search string: %s", e.getLocalizedMessage());
            System.out.println(errorMessage);
        }

        // example of Query search
        SpanQuery include = new SpanTermQuery(new Term("body", "nausea"));
        SpanQuery excludes[] = {
                new SpanTermQuery(new Term("body", "side")),
                new SpanTermQuery(new Term("body", "effects"))
        };
        int distanceBetweenExcludedTerms = 1;
        SpanNearQuery exclude = new SpanNearQuery(excludes, distanceBetweenExcludedTerms, true);
        int distanceBetweenInclusionAndExclusion = 15;
        Query notQuery = new SpanNotQuery(include, exclude, distanceBetweenInclusionAndExclusion);
        searcher.searchByQuery(notQuery);
    }

}

