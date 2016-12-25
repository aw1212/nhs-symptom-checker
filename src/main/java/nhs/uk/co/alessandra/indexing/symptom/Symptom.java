package nhs.uk.co.alessandra.indexing.symptom;

public class Symptom {

    private String ailment;
    private String symptom;

    public Symptom(String ailment, String symptom) {
        this.ailment = ailment;
        this.symptom = symptom;
    }

    public String getAilment() {
        return ailment;
    }

    public void setAilment(String ailment) {
        this.ailment = ailment;
    }

    public String getSymptom() {
        return symptom;
    }

    public void setSymptom(String symptom) {
        this.symptom = symptom;
    }
}
