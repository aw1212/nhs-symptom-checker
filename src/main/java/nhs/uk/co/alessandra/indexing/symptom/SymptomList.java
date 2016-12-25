package nhs.uk.co.alessandra.indexing.symptom;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
public class SymptomList {


    private static final String WIKIPEDIA_URL = "https://en.wikipedia.org/wiki/List_of_medical_symptoms";
    private static final String WIKIPEDIA_SELECTOR = ".wikitable li a";
    private static final String HEALTHLINE_URL_PREFIX = "http://www.healthline.com/directory/symptoms-";
    private static final String HEALTHLINE_SELECTOR = ".box-directory-list li a";
    private static Set<String> symptoms;
    private static Set<String> urls;

    public SymptomList() {
        symptoms = new HashSet<>();
        urls = new HashSet<>();
    }

    public Set<String> getSymptoms() {
        return symptoms;
    }

    public void initializeSymptomList() {
        getWikipediaSymptomsList();
        getHealthLineSymptomsList();
    }

    private void getWikipediaSymptomsList() {
        extractSymptomsFromUrl(WIKIPEDIA_URL, WIKIPEDIA_SELECTOR);
    }

    private void getHealthLineSymptomsList() {
        populateHealthLineUrlSet();
        for (String url : urls) {
            extractSymptomsFromUrl(url, HEALTHLINE_SELECTOR);
        }
    }

    private void populateHealthLineUrlSet() {
        for (char alphabet = 'a'; alphabet <= 'z'; alphabet++) {
            urls.add(HEALTHLINE_URL_PREFIX + alphabet);
        }
    }

    private void extractSymptomsFromUrl(String url, String selector) {
        try {
            Document document = Jsoup.connect(url).get();
            Elements elements = document.select(selector);
            symptoms.addAll(elements.stream()
                    .filter(element -> element != null)
                    .map(element -> " " + element.text().toLowerCase() + " ")
                    .collect(Collectors.toList()));
        } catch (IOException e) {
            String errorMessage = String.format("Problem connecting to %s: %s", url, e.getLocalizedMessage());
            System.out.println(errorMessage);
        }
    }

}
