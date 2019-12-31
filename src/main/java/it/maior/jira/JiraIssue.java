package it.maior.jira;

import com.atlassian.jira.rest.client.api.domain.Status;
import it.maior.jira.docx.BacklogItem;

import java.util.Map;
import java.util.Set;

public class JiraIssue implements BacklogItem {
    final private String phase;
    final private String epic;
    final private String title;
    final private String id;
    final private String description;
    final private Set<String> labels;
    final private Status status;

    final private Map<String, byte[]> attachments;

    final private boolean partOfSystemDesign;
    private final String acceptanceCriteria;

    public JiraIssue(String phase, String epic, String id, String title, String description, Map<String, byte[]> attachments, String acceptanceCriteria, boolean partOfSystemDesign, Set<String> labels, Status status) {
        this.phase = phase;
        this.epic = epic;
        this.title = title;
        this.id = id;
        this.description = description;
        this.attachments = attachments;
        this.partOfSystemDesign = partOfSystemDesign;
        this.acceptanceCriteria = acceptanceCriteria;
        this.labels = labels;
        this.status = status;
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

    @Override
    public String getAcceptanceCriteria() {
        return acceptanceCriteria;
    }

    public boolean isPartOfSystemDesign() {
        return partOfSystemDesign;
    }

    @Override
    public String toString() {
        return "JiraIssue{" +
                "epic='" + epic + '\'' +
                ", title='" + title + '\'' +
                '}';
    }

//    @Override
//    public String toString() {
//        return "JiraIssue{" +
//                "phase='" + phase + '\'' +
//                ", epic='" + epic + '\'' +
//                ", title='" + title + '\'' +
//                ", description='" + description + '\'' +
//                ", attachments=" + attachments +
//                '}';
//    }
}
