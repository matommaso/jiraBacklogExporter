package it.maior.jira;

import com.atlassian.jira.rest.client.api.domain.Status;
import it.maior.jira.docx.BacklogItem;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JiraIssue implements BacklogItem {
    final private String phase;
    final private String epic;
    final private String title;
    final private String id;
    final private String description;
    final private Set<String> labels;
    final private Status status;
    final private String sprintName;

    final private Map<String, byte[]> attachments;

    final private String icaroxtDocument;
    private final String acceptanceCriteria;

    public JiraIssue(String phase, String epic, String id, String title, String description, Map<String, byte[]> attachments, String acceptanceCriteria, String icaroxtDocument, Set<String> labels, Status status, String sprintName) {
        this.phase = phase;
        this.epic = epic;
        this.title = title;
        this.id = id;
        this.description = description;
        this.attachments = attachments;
        this.icaroxtDocument = icaroxtDocument;
        this.acceptanceCriteria = acceptanceCriteria;
        this.labels = labels;
        this.status = status;
        this.sprintName = sprintName;
    }

    public String getPhase() {
        return phase;
    }

    public String getEpic() {
        return epic;
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, byte[]> getAttachments() {
        return attachments;
    }

    public Set<String> getLabels() {
        return labels;
    }

    public String getStatus() {
        return status.getName();
    }

    public String getSprintName() {
        return sprintName;
    }


    public int getSprintNumber() {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(this.sprintName);
        int sprintNumber = 0;
        while (matcher.find()) {
            sprintNumber = Integer.parseInt(matcher.group(0));
        }
        return sprintNumber;
    }


    @Override
    public String getAcceptanceCriteria() {
        return acceptanceCriteria;
    }

    public String getIcaroXtDocument() {
        return icaroxtDocument;
    }

    @Override
    public String toString() {
        return "JiraIssue{" +
                "epic='" + epic + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
