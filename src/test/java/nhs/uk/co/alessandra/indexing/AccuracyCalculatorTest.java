package nhs.uk.co.alessandra.indexing;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import nhs.uk.co.alessandra.indexing.repository.NHSSymptomIndexerRepository;

@RunWith(MockitoJUnitRunner.class)
public class AccuracyCalculatorTest {

    @Mock
    private NHSSymptomIndexerRepository nhsSymptomIndexerRepository;
    @InjectMocks
    private AccuracyCalculator accuracyCalculator = new AccuracyCalculator();

    @Test
    public void givenAilment_whenCalculatingAccuracy_thenAccuracyForThatAilmentIsCalculated() {
        Ailment ailment = Ailment.FROZEN_SHOULDER;
        List<String> symptoms = new ArrayList<>(AccuracyCalculator.FROZEN_SHOULDER_SYMPTOMS);
        when(nhsSymptomIndexerRepository.getSymptomsByAilment(ailment.getName())).thenReturn(symptoms);

        long accuracy = accuracyCalculator.getAccuracy(ailment);
        assertEquals(100, accuracy);
    }

}