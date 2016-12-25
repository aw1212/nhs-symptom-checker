package nhs.uk.co.alessandra.indexing.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import nhs.uk.co.alessandra.indexing.lucene.LuceneIndexer;
import nhs.uk.co.alessandra.indexing.repository.NHSSymptomIndexerRepository;

public class NHSSiteIndexer {

    private static final int NUM_SPIDERS = 2;
    private static final int NUM_DOC_PROCESSORS = 2;
    private final Queue<String> startUrls;
    private final List<Runnable> spiders;
    private final BlockingQueue<Pair<String, Document>> documentsToProcess;
    private final List<Pair<String, String>> contents;
    private volatile boolean finished = false;
    private Set<String> symptoms;
    private NHSSymptomIndexerRepository nhsSymptomIndexerRepository;
    private LuceneIndexer luceneIndexer;

    public NHSSiteIndexer(Set<String> symptoms, NHSSymptomIndexerRepository nhsSymptomIndexerRepository, LuceneIndexer luceneIndexer) {
        startUrls = new ConcurrentLinkedQueue<>();
        spiders = new ArrayList<>();
        documentsToProcess = new LinkedBlockingQueue<>(NUM_DOC_PROCESSORS * 2);
        contents = new CopyOnWriteArrayList<>();
        this.symptoms = symptoms;
        this.nhsSymptomIndexerRepository = nhsSymptomIndexerRepository;
        this.luceneIndexer = luceneIndexer;
    }

    public void indexPages() {
        populateStartUrlsQueue();
        ExecutorService executorService = Executors.newFixedThreadPool(NUM_SPIDERS + NUM_DOC_PROCESSORS);
        for (int i = 0; i < NUM_SPIDERS; i++) {
            Runnable spider = new NHSSpider();
            spiders.add(spider);
            executorService.execute(spider);
        }
        for (int i = 0; i < NUM_DOC_PROCESSORS; i++) {
            Runnable worker = new NHSDocumentProcessor();
            executorService.execute(worker);
        }
        executorService.shutdown();
        while (!executorService.isTerminated()) {
        }

        luceneIndexer.createIndexAndAddDocuments(contents);
    }

    private void populateStartUrlsQueue() {
        List<String> nhsUrls = nhsSymptomIndexerRepository.getStartUrls();
        startUrls.addAll(nhsUrls);
    }

    private class NHSSpider implements Runnable {

        @Override
        public void run() {
            String startUrl;
            while (!finished && (startUrl = startUrls.poll()) != null) {
                try {
                    spiderPage(startUrl);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                if (Thread.interrupted()) {
                    finished = true;
                }
            }
        }

        private void spiderPage(String startUrl) throws InterruptedException {
            try {
                Document document = Jsoup.connect(startUrl).get();
                Pair<String, Document> urlDocumentPair = Pair.of(startUrl, document);
                documentsToProcess.put(urlDocumentPair);
            } catch (IOException e) {
                String errorMessage = String.format("Problem connecting to url %s: %s", startUrl, e.getLocalizedMessage());
                System.out.println(errorMessage);
            }
        }

    }

    private class NHSDocumentProcessor implements Runnable {
        static final int TIMEOUT = 2000;

        @Override
        public void run() {
            while(!(finished && documentsToProcess.isEmpty())) {
                try {
                    Pair<String, Document> urlDocumentPair = documentsToProcess.poll(TIMEOUT, TimeUnit.MILLISECONDS);
                    if (urlDocumentPair != null) {
                        processUrl(urlDocumentPair);
                    } else if (isSpideringFinished() && documentsToProcess.isEmpty()) {
                        finished = true;
                    }
                } catch (InterruptedException e) {
                    return;
                }
            }
        }

        private void processUrl(Pair<String, Document> urlDocumentPair) {
            Document doc = urlDocumentPair.getRight();
            String url = urlDocumentPair.getLeft();
            Element element = doc.getElementsByAttributeValueContaining("class", "main-content healthaz-content clear").first();
            String content = element.text();
            Pair<String, String> urlPageContentPair = Pair.of(url, content);
            contents.add(urlPageContentPair);
            saveSymptomsFromPage(content, url);
            nhsSymptomIndexerRepository.markStartPathsAsVisited(urlDocumentPair.getLeft());
        }

        private void saveSymptomsFromPage(String content, String url) {
            Set<String> matches = symptoms.stream()
                    .filter(symptom -> content.toLowerCase().contains(symptom))
                    .map(String::trim)
                    .collect(Collectors.toSet());
            String ailment = getNameOfAilmentFromURL(url);
            matches.forEach(symptom -> nhsSymptomIndexerRepository.addSymptom(ailment, symptom));
        }

        private String getNameOfAilmentFromURL(String url) {
            String tail = url.toLowerCase().replace("http://www.nhs.uk/conditions/", "");
            return tail.toLowerCase().replace("/pages/symptoms.aspx", "");
        }

        private boolean isSpideringFinished() {
            for (Runnable spider : spiders) {
                Thread spiderThread = new Thread(spider);
                if (spiderThread.isAlive()) {
                    return false;
                }
            }
            return !spiders.isEmpty();
        }
    }

}
