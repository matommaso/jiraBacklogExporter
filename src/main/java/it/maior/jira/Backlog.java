package it.maior.jira;

import com.google.common.collect.Lists;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Backlog {
    private List<Epic> epics = new LinkedList<>();

    public List<Epic> getEpics() {
        return epics;
    }

//    public void addIssue(JiraIssue jiraIssue) {
//        final Optional<Epic> epic = epics.stream().filter(e -> e.getTitle().equals(jiraIssue.getEpic())).findFirst();
//        if (epic.isPresent()) {
//            epic.get().addIssue(jiraIssue);
//        } else {
//            epics.add(new Epic(jiraIssue.getEpic(), Lists.newArrayList(jiraIssue), jiraIssue.getEpic(), jiraIssue.getId(), jiraIssue.getAcceptanceCriteria()));
//            epics.sort(Comparator.comparing(Epic::getTitle));
//        }
//    }
//
//    public void mergeWith(Backlog backlog2) {
//        backlog2.getEpics().stream()
//                .forEach(epic2 -> {
//                    final Optional<Epic> epic = getEpic(epic2);
//                    epic.ifPresentOrElse(
//                            e -> e.getIssues().addAll(epic2.getIssues()),
//                            () -> epics.add(epic2));
//                });
//    }
//
//    private Optional<Epic> getEpic(Epic epic2) {
//        return epics.stream().filter(epic -> epic.getTitle().equals(epic2.getTitle())).findFirst();
//    }
//
//    public void addEpic(Epic epic) {
//        epics.add(epic);
//    }

    public void setEpics(List<Epic> epics) {
        this.epics = epics;
    }
}
