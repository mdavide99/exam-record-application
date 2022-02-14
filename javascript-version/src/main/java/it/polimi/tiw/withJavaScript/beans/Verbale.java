package it.polimi.tiw.withJavaScript.beans;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Object define the structure of the report for JSON serialization
 *
 */
public class Verbale {
	
	private int reportId;
	private String dateTime;
	private ExamSession examSession;
	private List<Student> students = new ArrayList<>();
	private List<String> results = new ArrayList<>();
	
	public int getReportId() {
		return reportId;
	}
	public void setReportId(int reportId) {
		this.reportId = reportId;
	}
	public String getDateTime() {
		return dateTime;
	}
	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}
	public ExamSession getExamSession() {
		return examSession;
	}
	public void setExamSession(ExamSession examSession) {
		this.examSession = examSession;
	}
	public List<Student> getStudents() {
		return students;
	}
	public void setStudents(List<Student> students) {
		this.students = students;
	}
	public List<String> getResults() {
		return results;
	}
	public void setResults(List<String> results) {
		this.results = results;
	}
	
	
	
}