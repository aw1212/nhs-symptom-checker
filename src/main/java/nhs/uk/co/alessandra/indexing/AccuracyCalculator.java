package nhs.uk.co.alessandra.indexing;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.google.common.collect.ImmutableSet;

import nhs.uk.co.alessandra.indexing.repository.NHSSymptomIndexerRepository;

@Component
public class AccuracyCalculator {

    public static final Set<String> BRONCHITIS_SYMPTOMS =
            ImmutableSet.of("hacking cough", "phlegm", "sore throat", "headache", "runny or blocked nose", "aches", "pains", "tiredness");
    public static final Set<String> HEART_BLOCK_SYMPTOMS = ImmutableSet.of("light-headedness", "dizziness", "fainting", "chest pain",
            "shortness of breath", "tiring easily", "unusually pale and blotchy skin", "lethargy", "fatigue", "bradycardia");
    public static final Set<String> FROZEN_SHOULDER_SYMPTOMS = ImmutableSet.of("pain", "stiffness", "ache");
    public static final Set<String> CORONARY_HEART_DISEASE_SYMPTOMS = ImmutableSet.of("chest pain", "heart attack", "heart palpitations",
            "breathlessness", "angina");

    @Autowired
    private NHSSymptomIndexerRepository nhsSymptomIndexerRepository;

    public long getAccuracy(Ailment ailment) {
        String ailmentName = ailment.getName();
        List<String> extractedSymptoms = nhsSymptomIndexerRepository.getSymptomsByAilment(ailmentName);
        Set<String> symptoms = getSymptomSet(ailment);
        long matches = extractedSymptoms.stream()
                .filter(symptoms::contains)
                .count();
        return (matches * 100) / symptoms.size();
    }

    private Set<String> getSymptomSet(Ailment ailment) {
        switch (ailment) {
            case BRONCHITIS:
                return BRONCHITIS_SYMPTOMS;
            case HEART_BLOCK:
                return HEART_BLOCK_SYMPTOMS;
            case FROZEN_SHOULDER:
                return FROZEN_SHOULDER_SYMPTOMS;
            case CORONARY_HEART_DISEASE:
                return CORONARY_HEART_DISEASE_SYMPTOMS;
            default:
                System.out.println("Invalid ailment");
                break;
        }
        return new HashSet<>();
    }

}
