package it.maior.jira.docx;

import it.maior.docx.DocxFileCreator;
import it.maior.jira.Epic;
import it.maior.jira.JiraIssue;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EpicTestPlanExporter {
    private final Epic epic;
    private final DocxFileCreator docxFileCreator;
    private final Predicate<JiraIssue> jiraIssuePredicate;

    public EpicTestPlanExporter(Epic epic, DocxFileCreator docxFileCreator, Predicate<JiraIssue> jiraIssuePredicate) {
        this.epic = epic;
        this.docxFileCreator = docxFileCreator;
        this.jiraIssuePredicate = jiraIssuePredicate;
    }

    public void export() {

        if(getFilteredJiraIssues().size() > 0) {
            docxFileCreator.writeTitle(epic.getTitle());

            docxFileCreator.addParagraphWithConfiguration(epic.getAcceptanceCriteria());
        }
    }

    private List<JiraIssue> getFilteredJiraIssues() {
        return epic.getIssues().stream()
                .filter(issue -> issue.isPartOfSystemDesign())
                .filter(jiraIssuePredicate).collect(Collectors.toList());
    }
}
