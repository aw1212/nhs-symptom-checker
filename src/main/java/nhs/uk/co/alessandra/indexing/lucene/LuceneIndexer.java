package nhs.uk.co.alessandra.indexing.lucene;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LuceneIndexer {

    @Autowired
    private Directory directory;

    public void createIndexAndAddDocuments(List<Pair<String, String>> titleBodyPairs) {
        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        try {
            IndexWriter writer = new IndexWriter(directory, config);
            for (Pair<String, String> titleBodyPair : titleBodyPairs) {
                String url = titleBodyPair.getLeft();
                String body = titleBodyPair.getRight();
                addDoc(writer, url, body);
            }
            writer.close();
        } catch (IOException e) {
            String errorMessage = String.format("Problem creating IndexWriter: %s", e.getLocalizedMessage());
            System.out.println(errorMessage);
        }
    }

    private void addDoc(IndexWriter w, String url, String body) throws IOException {
        Document doc = new Document();
        doc.add(new StringField("url", url, Field.Store.YES)); //not tokenized. needs exact match
        doc.add(new TextField("body", body, Field.Store.YES)); //tokenized
        w.addDocument(doc);
    }

}
