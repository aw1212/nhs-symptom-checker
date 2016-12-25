package nhs.uk.co.alessandra.indexing.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import nhs.uk.co.alessandra.indexing.symptom.SymptomList;
import nhs.uk.co.alessandra.indexing.crawler.NHSSiteIndexer;
import nhs.uk.co.alessandra.indexing.lucene.LuceneIndexer;
import nhs.uk.co.alessandra.indexing.repository.NHSSymptomIndexerRepository;

@Component
public class SiteIndexerFactory {

    @Autowired
    private NHSSymptomIndexerRepository NHSSymptomIndexerRepository;
    @Autowired
    private SymptomList symptomList;
    @Autowired
    private LuceneIndexer luceneIndexer;

    public void startSiteIndexer() {
        symptomList.initializeSymptomList();
        NHSSiteIndexer nhsSiteIndexer = new NHSSiteIndexer(symptomList.getSymptoms(), NHSSymptomIndexerRepository, luceneIndexer);
        nhsSiteIndexer.indexPages();
    }

}
