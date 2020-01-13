package it.maior.jira.docx;

import it.maior.docx.DocxFileCreator;
import it.maior.jira.Epic;
import it.maior.jira.JiraIssue;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EpicDocxExporter {
    private final DocxFileCreator docxFileCreator;
    private final Epic epic;
    private final Predicate<JiraIssue> jiraIssuePredicate;

    public EpicDocxExporter(Epic epic, DocxFileCreator docxFileCreator, Predicate<JiraIssue> jiraIssuePredicate) {
        this.epic = epic;
        this.docxFileCreator = docxFileCreator;
        this.jiraIssuePredicate = jiraIssuePredicate;
    }

    public void export() {

        final List<JiraIssue> filteredJiraIssues = getFilteredJiraIssues();
        if(filteredJiraIssues.size() > 0) {
            docxFileCreator.writeTitle(epic.getTitle());

            filteredJiraIssues.stream()
                    .forEach(issue -> {
                        final BacklogItemDocxExporter backlogItemDocxExporter = new BacklogItemDocxExporter(issue, docxFileCreator);
                        backlogItemDocxExporter.export();

                        docxFileCreator.addEmptyLine();
                    });
        }
    }

    private List<JiraIssue> getFilteredJiraIssues() {
        return epic.getIssues().stream()
                .filter(issue -> issue.getIcaroXtDocument().equals("System Design"))
                    .filter(jiraIssuePredicate).collect(Collectors.toList());
    }
}
