package it.polimi.tiw.purehtml.beans;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Object that reflect the structure of the exam sessions from the database
 *
 */
public class ExamSession {

    private int sessionId;
    private String courseName;
    private String sessionDate;
    private List<String> states = new ArrayList<>();
    private List<String> results = new ArrayList<>();
    private List<Student> subscribers = new ArrayList<>();

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(String sessionDate) {
        this.sessionDate = sessionDate;
    }

    public List<String> getStates() {
        return states;
    }

    public void setStates(List<String> states) {
        this.states = states;
    }

    public List<String> getResults() {
        return results;
    }

    public void setResults(List<String> results) {
        this.results = results;
    }

    public List<Student> getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(List<Student> subscribers) {
        this.subscribers = subscribers;
    }
}
