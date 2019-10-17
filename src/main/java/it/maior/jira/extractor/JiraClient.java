package it.maior.jira.extractor;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.*;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import it.maior.jira.Backlog;
import it.maior.jira.Epic;
import it.maior.jira.JiraIssue;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class JiraClient {
    public static final String QUERY_TO_FIND_ALL_ISSUES_IN_ICAROXT_PROJECT = "project = IC and issuetype in (Story, Bug) ";
    public static final String QUERY_TO_FIND_ALL_ISSUES_IN_ICAROXT_EPIC = "project = IC and issuetype in (Story, Bug) and \"Epic Link\" = {0} Order By RANK";
    public static final String QUERY_TO_FIND_ALL_EPICS_IN_ICAROXT_PROJECT = "project = IC and issuetype = Epic Order By RANK"; // and "Epic Status" = Done
    public static final String SYSTEM_DESIGN = "System Design";

    private final String username;
    private final String password;
    private final String jiraUrl;
    private final JiraRestClient restClient;
    private final IssueRestClient issueClient;

    private final ObjectMapper mapper = new ObjectMapper();

    public JiraClient(String username, String password, String jiraUrl) {
        this.username = username;
        this.password = password;
        this.jiraUrl = jiraUrl;
        this.restClient = new AsynchronousJiraRestClientFactory()
                .createWithBasicHttpAuthentication(URI.create(this.jiraUrl), this.username, this.password);

        issueClient = restClient.getIssueClient();
    }

//    public Backlog _retrieveBacklog() {
//        final List<Issue> issues = retrieveIssues();
//
//        return issues.stream()
//                .parallel()
//                .map(issue -> toJiraIssue(issue, ""))
//                .sorted(Comparator.comparing(JiraIssue::getEpic))
//                .collect(() -> new Backlog(), (backlog, epic) -> backlog.addIssue(epic), (backlog1, backlog2) -> backlog1.mergeWith(backlog2));
//
//    }

    public Backlog retrieveBacklog() {
        final List<Issue> epics = retrieveEpics();

        final Backlog backlog = new Backlog();

        final List<Epic> jiraEpics = epics.stream()
                .parallel()
                .map(epic -> toJiraEpic(epic))
                .map(jiraEpic -> retrieveStories(jiraEpic))
                .collect(Collectors.toList());

        backlog.setEpics(jiraEpics);

        return backlog;
    }

    private Epic retrieveStories(Epic jiraEpic) {
        SearchResult initialSearchResult = null;
        do {
            try {
                initialSearchResult = restClient.getSearchClient().searchJql(MessageFormat.format(QUERY_TO_FIND_ALL_ISSUES_IN_ICAROXT_EPIC, jiraEpic.getKey()), 1000, 0, null).claim();
            } catch (Exception e) {
                System.out.println("Retrying getting all stories for epic:  " + jiraEpic.getKey());
                sleep(300);
            }
        } while(initialSearchResult == null);

        final List<JiraIssue> issues = StreamSupport.stream(initialSearchResult.getIssues().spliterator(), false)
                .parallel()
                .map(issue -> toJiraIssue(issue, jiraEpic.getTitle()))
                .collect(Collectors.toList());

        jiraEpic.setIssues(issues);
        return jiraEpic;
    }

    private Epic toJiraEpic(Issue epic) {
        final String epicName = getCustomFieldValue(epic, "Epic Name", f -> extractString(f.getValue()));
        final String acceptanceCriteria = getCustomFieldValue(epic, "Acceptance Criteria", f -> extractString(f.getValue()));

        return new Epic(epicName, epic.getKey(), epic.getKey(), acceptanceCriteria);
    }

    private List<Issue> retrieveEpics() {
        final SearchResult initialSearchResult = restClient.getSearchClient().searchJql(QUERY_TO_FIND_ALL_EPICS_IN_ICAROXT_PROJECT, 1000, 0, null).claim();
        return StreamSupport.stream(initialSearchResult.getIssues().spliterator(), false)
                .collect(Collectors.toList());
    }

    private List<Issue> retrieveIssues() {
        final List<Issue> issues = new ArrayList<>();

        final Pair<SearchResult, Issue> initialSearchResultAndIssue = retrieveSearchResultAndIssue(0);
        issues.add(initialSearchResultAndIssue.getRight());

        IntStream.range(1, initialSearchResultAndIssue.getLeft().getTotal()).parallel().forEach(i -> issues.add(retrieveSearchResultAndIssue(i).getRight()));

        return issues;
    }

    private Pair<SearchResult, Issue> retrieveSearchResultAndIssue(int indexOfIssueToRetrive) {
        final SearchResult initialSearchResult = restClient.getSearchClient().searchJql(QUERY_TO_FIND_ALL_ISSUES_IN_ICAROXT_PROJECT, 1, indexOfIssueToRetrive, null).claim();
        return Pair.of(initialSearchResult, initialSearchResult.getIssues().iterator().next());
    }

    private JiraIssue toJiraIssue(Issue issue, String epicTitle) {
        final String phase = getCustomFieldValue(issue, "Phase", f -> getValueFromJson(f.getValue()));
        final String key = issue.getKey();
        final String title = issue.getSummary();
        final String description = issue.getDescription();
        final String acceptanceCriteria = getCustomFieldValue(issue, "Acceptance Criteria", f -> extractString(f.getValue()));

        final Map<String, byte[]> attachments = extractAttachments(issue);

        final boolean partOfSystemDesign = getCustomFieldValue(issue, "Is part of the following official documents", f -> getValueFromJson(f.getValue())).equals(SYSTEM_DESIGN);
        return new JiraIssue(phase, epicTitle, key, title, description, attachments, acceptanceCriteria, partOfSystemDesign);
    }

    private Map<String, byte[]> extractAttachments(Issue issue) {
        Map<String, byte[]> collect = new HashMap<>();
        Issue claim = null;
        do {
            try {
                claim = issueClient.getIssue(issue.getKey())
                        .claim();

            } catch (Exception e) {
                System.out.println("Retrying claiming " + issue.getKey());
                sleep(300);
            }
        } while(claim == null);

        do {
            try {
                collect = stream(claim.getAttachments())
                        .map(attachment -> attachmentsToIssueAttachment(attachment))
                        .collect(Collectors.toMap(issueAttachment -> issueAttachment.getName(), issueAttachment -> issueAttachment.getStream()));
            } catch (Exception e) {
                System.out.println("Retrying attachments " + issue.getKey());
                sleep(300);
            }
        } while(collect == null);
        return collect;
    }

    private void sleep(int i)  {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String getCustomFieldValue(Issue issue, String name, Function<IssueField, Optional<String>> fieldMapper) {
        return Optional.ofNullable(issue.getFieldByName(name)).flatMap(fieldMapper).orElse("");
    }

    private Optional<String> extractString(Object value) {
        if(value == null) {
            return Optional.empty();
        }

        return Optional.of((String) value);
    }

    private Optional<String> getValueFromJson(Object json) {
        return Optional.ofNullable(json).flatMap(o -> {
            try {
                JsonNode jsonNode = getJsonNode(o);
                return Optional.of(jsonNode.get("value").asText());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return Optional.empty();
        });
    }

    private JsonNode getJsonNode(Object o) throws IOException {
        final JsonNode jsonNode = mapper.readTree(o.toString());
        if(jsonNode instanceof ArrayNode) {
            return jsonNode.get(0);
        }
        return jsonNode;
    }

    private <T> Stream<T> stream(Iterable<T> iterable) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        iterable.iterator(),
                        Spliterator.ORDERED
                ),
                false
        );
    }

    private IssueAttachment attachmentsToIssueAttachment(Attachment a) {
        final String name = a.getFilename();

        final InputStream stream = issueClient.getAttachment(a.getContentUri()).claim();

        try {
            return new IssueAttachment(name, IOUtils.toByteArray(stream));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    class IssueAttachment {
        private String name;

        private byte[] stream;

        public IssueAttachment(String name, byte[] stream) {
            this.name = name;
            this.stream = stream;
        }

        public String getName() {
            return name;
        }

        public byte[] getStream() {
            return stream;
        }
    }
}
