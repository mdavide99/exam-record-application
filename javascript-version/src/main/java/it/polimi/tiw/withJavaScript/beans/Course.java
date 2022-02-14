package it.polimi.tiw.withJavaScript.beans;

import java.util.List;

/**
 * 
 * Object that reflect the structure of the courses from the database
 *
 */
public class Course {

    private String courseName;
    private int teacherId;
    private List<ExamSession> examSessionList;

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public List<ExamSession> getExamSessionList() {
        return examSessionList;
    }

    public void setExamSessionList(List<ExamSession> examSessionList) {
        this.examSessionList = examSessionList;
    }
}
