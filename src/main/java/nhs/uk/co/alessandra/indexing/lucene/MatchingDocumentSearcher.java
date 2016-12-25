package nhs.uk.co.alessandra.indexing.lucene;

import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.complexPhrase.ComplexPhraseQueryParser;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.surround.parser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MatchingDocumentSearcher {

    @Autowired
    private Directory directory;

    private IndexReader reader;
    private IndexSearcher searcher;

    public void searchByQueryString(String queryString)
            throws ParseException, IOException, QueryNodeException, org.apache.lucene.queryparser.classic.ParseException {
        StandardAnalyzer analyzer = new StandardAnalyzer();
        ComplexPhraseQueryParser queryParser = new ComplexPhraseQueryParser("body", analyzer);
        Query query = queryParser.parse(queryString);
        ScoreDoc[] hits = getHits(query);

        System.out.println("Query string: " + queryString );
        printMessage(hits);
        reader.close();
    }

    public void searchByQuery(Query query) throws IOException {
        ScoreDoc[] matches = getHits(query);

        System.out.println("Query type: " + query.getClass().getSimpleName());
        printMessage(matches);
        reader.close();
    }

    private ScoreDoc[] getHits(Query query) throws IOException {
        reader = DirectoryReader.open(directory);
        searcher = new IndexSearcher(reader);
        TopScoreDocCollector collector = TopScoreDocCollector.create(10, null);
        searcher.search(query, collector);
        return collector.topDocs().scoreDocs;
    }

    private void printMessage(ScoreDoc[] matches) throws IOException {
        System.out.println("Found " + matches.length + " hit(s).");
        for (int i = 0; i < matches.length; ++i) {
            int docId = matches[i].doc;
            Document doc = searcher.doc(docId);
            System.out.println((i + 1) + ". " + doc.get("url") + "\t" + doc.get("body"));
        }
        System.out.println("");
    }

}
