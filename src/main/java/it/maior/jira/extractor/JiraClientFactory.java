package it.maior.jira.extractor;

public class JiraClientFactory {
    public static JiraClient createMyJiraClient() {
        final String username = System.getProperty("jiraUsername");//"tommaso.magherini@maior.it";
        final String password = System.getProperty("jiraApiToker");//"VeUgni48lXfAbAjKwClg9E35"; //System.getProperty("jiraPassword");

        return new JiraClient(
                username,
                password,
                "https://maiorprojects.atlassian.net");
    }

}
