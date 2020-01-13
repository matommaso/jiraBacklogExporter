package it.maior.jira.docx;

import it.maior.docx.DocxFileCreator;
import it.maior.jira.Epic;
import it.maior.jira.JiraIssue;
import it.maior.jira.application.TestPlanConditions;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EpicTestPlanExporter {
    private final Epic epic;
    private final DocxFileCreator docxFileCreator;
    private final Predicate<JiraIssue> jiraIssuePredicate;
    private final TestPlanConditions testPlanConditions;

    public EpicTestPlanExporter(Epic epic, DocxFileCreator docxFileCreator, TestPlanConditions testPlanConditions, Predicate<JiraIssue> jiraIssuePredicate) {
        this.epic = epic;
        this.docxFileCreator = docxFileCreator;
        this.testPlanConditions = testPlanConditions;
        this.jiraIssuePredicate = jiraIssuePredicate;
    }

    public void export() {
        final List<JiraIssue> filteredJiraIssues = getFilteredJiraIssues();

        if (filteredJiraIssues.size() > 0) {
            docxFileCreator.writeTitle(epic.getTitle());

            filteredJiraIssues.stream()
                    .forEach(issue -> {
                        final BacklogItemDocxExporter backlogItemDocxExporter = new BacklogItemDocxExporter(issue, docxFileCreator);
                        backlogItemDocxExporter.export();
                        docxFileCreator.addEmptyLine();
                    });
        }
        docxFileCreator.addParagraphWithConfiguration(epic.getAcceptanceCriteria());

    }

    private boolean getFilteredJiraIssues(JiraIssue issue) {
        return
                issue.getStatus().equals(testPlanConditions.getIssueStatus())
                        && issue.getSprintNumber() >= testPlanConditions.getFirstSprintNumber()
                        && issue.getSprintNumber() <= testPlanConditions.getLastSprintNumber()
                      //  && issue.getIcaroXtDocument().equals(testPlanConditions.getIssueIcaroXtDocument())
                ;
    }

    private List<JiraIssue> getFilteredJiraIssues() {
        return epic.getIssues().stream()
                .filter(issue -> getFilteredJiraIssues(issue))
                .filter(jiraIssuePredicate)
                .collect(Collectors.toList());
    }
}
