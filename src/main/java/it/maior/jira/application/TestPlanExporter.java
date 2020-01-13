package it.maior.jira.application;

import it.maior.docx.DocxFileCreator;
import it.maior.docx.ParagraphStyle;
import it.maior.jira.Backlog;
import it.maior.jira.JiraIssue;
import it.maior.jira.docx.EpicTestPlanExporter;
import it.maior.jira.extractor.JiraClientFactory;

import java.util.Arrays;

import static it.maior.jira.exporter.Phase.*;

public class TestPlanExporter {

    public static void main(String[] args) {

        final TestPlanConditions testPlanConditions = new TestPlanConditions();

        final Backlog backlog = JiraClientFactory.createMyJiraClient().retrieveBacklog();

        final DocxFileCreator docxFileCreator = new DocxFileCreator();

        Arrays.asList(FIRST_YEAR, SECOND_YEAR, THIRD_YEAR, TO_BE_DECIDED, TO_BE_ASSIGNED).stream().forEach(
                phase -> {
                    writePhase(docxFileCreator, phase.getDescription());
                    backlog.getEpics().stream()
                            .forEach(epic -> new EpicTestPlanExporter(epic, docxFileCreator,testPlanConditions, (JiraIssue i) -> i.getPhase().equals(phase.getValue())).export());
                }
        );

        docxFileCreator.saveFileDocx("./output/testPlan.docx");
    }

    private static void writePhase(DocxFileCreator docxFileCreator, String phase) {
        docxFileCreator.addParagraph(docxFileCreator.createStyledParagraphOfText(ParagraphStyle.HEADING1.getStyle(), phase));
    }
}
