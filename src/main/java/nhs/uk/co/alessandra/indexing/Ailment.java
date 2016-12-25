package nhs.uk.co.alessandra.indexing;

public enum Ailment {

    BRONCHITIS("bronchitis"),
    HEART_BLOCK("heart-block"),
    FROZEN_SHOULDER("frozen-shoulder"),
    CORONARY_HEART_DISEASE("coronary-heart-disease");

    private String name;

    Ailment(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
