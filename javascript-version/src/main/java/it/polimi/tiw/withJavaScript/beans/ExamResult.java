package it.polimi.tiw.withJavaScript.beans;

/**
 * 
 * Object define the structure of the result for JSON serialization
 *
 */
public class ExamResult {
	
	private Student student;
	private String result;
	private String state;
	private boolean isRefusable;
	private ExamSession examSession;
	
	public Student getStudent() {
		return student;
	}
	public void setStudent(Student student) {
		this.student = student;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public boolean isRefusable() {
		return isRefusable;
	}
	public void setRefusable(boolean isRefusable) {
		this.isRefusable = isRefusable;
	}
	public ExamSession getExamSession() {
		return examSession;
	}
	public void setExamSession(ExamSession examSession) {
		this.examSession = examSession;
	}
	
	
	
	
}