package it.maior.jira.application;

public class TestPlanConditions {
    private final int firstSprintNumber = 0;
    private final int lastSprintNumber = 25;
    private final String issueStatus = "Done";
    private  final String issueIcaroXtDocument = "System Design";

    public String getIssueIcaroXtDocument() {
        return issueIcaroXtDocument;
    }

    public int getFirstSprintNumber() {
        return firstSprintNumber;
    }

    public int getLastSprintNumber() {
        return lastSprintNumber;
    }

    public String getIssueStatus() {
        return issueStatus;
    }
}
