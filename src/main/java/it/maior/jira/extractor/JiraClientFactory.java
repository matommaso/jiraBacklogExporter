package it.maior.jira.extractor;

public class JiraClientFactory {
    public static JiraClient createMyJiraClient() {
        final String username = System.getProperty("jiraUsername");
        final String password = System.getProperty("jiraPassword");

        return new JiraClient(
                username,
                password,
                "https://maiorprojects.atlassian.net");
    }

}
