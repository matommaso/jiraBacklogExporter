package it.maior.jira;

import it.maior.jira.docx.BacklogItem;
import com.atlassian.jira.rest.client.api.domain.Status;
import java.util.*;

public class Epic implements BacklogItem {

    private static final String EMPTY_STRING = "";

    private List<JiraIssue> issues;
    private final String title;
    private final String id;
    private final String key;
    private final String acceptanceCriteria;

    public Epic(String title, List<JiraIssue> issues, String key, String id, String acceptanceCriteria) {
        this.issues = issues;
        this.title = title;
        this.key = key;
        this.id = id;
        this.acceptanceCriteria = acceptanceCriteria;
    }

    public Epic(String title, String key, String id, String acceptanceCriteria) {
        this.title = title;
        this.key = key;
        this.id = id;
        this.acceptanceCriteria = acceptanceCriteria;
    }

    public String getKey() {
        return key;
    }

    public List<JiraIssue> getIssues() {
        return issues;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String getDescription() {
        return EMPTY_STRING;
    }

    @Override
    public Map<String, byte[]> getAttachments() {
        return new HashMap<>();
    }

    public void addIssue(JiraIssue jiraIssue) {
        issues.add(jiraIssue);
    }

    public void setIssues(List<JiraIssue> issues) {
        this.issues = issues;
    }

    public String getAcceptanceCriteria() {
        return acceptanceCriteria;
    }

    @Override
    public Set<String> getLabels() {
        return new HashSet<String>();
    }

    @Override
    public String getId() {
        return id;
    }

    public String getStatus() {
        return ""; //FiXME
    }
}
