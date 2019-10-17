package it.maior.jira.application;

import it.maior.docx.DocxFileCreator;
import it.maior.docx.ParagraphStyle;
import it.maior.jira.Backlog;
import it.maior.jira.JiraIssue;
import it.maior.jira.docx.EpicDocxExporter;

import it.maior.jira.extractor.JiraClientFactory;

import java.util.Arrays;

import static it.maior.jira.exporter.Phase.*;

public class SystemDesignExporter {

    public static void main(String[] args) {

        final Backlog backlog =  JiraClientFactory.createMyJiraClient().retrieveBacklog();

        final DocxFileCreator docxFileCreator = new DocxFileCreator();

        Arrays.asList(FIRST_YEAR, SECOND_YEAR, THIRD_YEAR, TO_BE_DECIDED, TO_BE_ASSIGNED).stream().forEach(
                phase -> {
                    writePhase(docxFileCreator, phase.getDescription());
                    backlog.getEpics().stream()
                            .forEach(epic -> new EpicDocxExporter(epic, docxFileCreator, (JiraIssue i) -> i.getPhase().equals(phase.getValue())).export());
                }
        );


        docxFileCreator.saveFileDocx("./output/backlog.docx");
    }

    private static void writePhase(DocxFileCreator docxFileCreator, String phase) {
        docxFileCreator.addParagraph(docxFileCreator.createStyledParagraphOfText(ParagraphStyle.HEADING1.getStyle(), phase));
    }
}
