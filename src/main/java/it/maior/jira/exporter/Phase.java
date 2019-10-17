package it.maior.jira.exporter;

public enum Phase {
    FIRST_YEAR("First year - ACS.XT", "ACS.XT"),
    SECOND_YEAR("Second year - ACR.XT", "ACR.XT"),
    THIRD_YEAR("Third year - ACM.XT", "ACM.XT"),

    TO_BE_DECIDED("To be decided", "NN"),
    TO_BE_ASSIGNED("To be assigned", "");

    private String description;

    private String value;

    Phase(String description, String value) {
        this.description = description;
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public String getValue() {
        return value;
    }
}
