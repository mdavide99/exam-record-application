package it.polimi.tiw.withJavaScript.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.withJavaScript.beans.ExamSession;
import it.polimi.tiw.withJavaScript.beans.Student;

/**
 * 
 * DAO used to handle interactions with the database regarding exam sessions
 *
 */
public class ExamSessionsDAO {
    private Connection con;

    public ExamSessionsDAO(Connection con) {
        this.con = con;
    }

    /**
     * 
     * Returns the exam session linked to the given id
     * 
     */
    public ExamSession getExamSession(int id) throws SQLException{
        String query = "SELECT * FROM examsessions E WHERE E.SessionId = ?";
        ExamSession examSession = new ExamSession();
        ResultSet result = null;
        PreparedStatement pstatement = null;
        try {
            pstatement = con.prepareStatement(query);
            pstatement.setString(1, String.valueOf(id));
            result = pstatement.executeQuery();
            while (result.next()) {
                examSession.setSessionId(result.getInt("SessionId"));
                examSession.setCourseName(result.getString("CourseName"));
                examSession.setSessionDate(result.getString("SessionDate"));
            }
        } catch (SQLException e) {
            throw new SQLException(e);
        }finally {
            try {
                result.close();
            } catch (Exception e1) {
                throw new SQLException("Cannot close result");
            }
            try {
                pstatement.close();
            } catch (Exception e1) {
                throw new SQLException("Cannot close statement");
            }
        }
        return examSession;
    }

    /**
     * 
     * Gets the result of a certain student in a certain exam session
     * 
     */
    public String getResult(int SessionID, int StudentSerialNumber) throws SQLException{
        String grade = null;
        String query = "SELECT R.Result FROM examsessions E, examresult R  WHERE R.StudentSerialNumber = ? AND E.SessionId = ? AND R.SessionId=E.SessionId";
        ResultSet result = null;
        PreparedStatement pstatement = null;
        try {
            pstatement = con.prepareStatement(query);
            pstatement.setString(1, String.valueOf(StudentSerialNumber));
            pstatement.setString(2, String.valueOf(SessionID));
            result = pstatement.executeQuery();
            while (result.next()) {
                grade = result.getString("Result");
            }
        } catch (SQLException e) {
            throw new SQLException(e);
        }finally {
            try {
                result.close();
            } catch (Exception e1) {
                throw new SQLException("Cannot close result");
            }
            try {
                pstatement.close();
            } catch (Exception e1) {
                throw new SQLException("Cannot close statement");
            }
        }
        return grade;
    }

    /**
     * 
     * Gets the current state of the exam's result 
     * 
     */
    public String getState(int SessionID, int StudentSerialNumber) throws SQLException{
        String state = null;
        String query = "SELECT R.State FROM examsessions E, examresult R  WHERE R.StudentSerialNumber = ? AND E.SessionId = ? AND R.SessionId=E.SessionId";
        ResultSet result = null;
        PreparedStatement pstatement = null;
        try {
            pstatement = con.prepareStatement(query);
            pstatement.setString(1, String.valueOf(StudentSerialNumber));
            pstatement.setString(2, String.valueOf(SessionID));
            result = pstatement.executeQuery();
            while (result.next()) {
                state = result.getString("State");
            }
        } catch (SQLException e) {
            throw new SQLException(e);
        }finally {
            try {
                result.close();
            } catch (Exception e1) {
                throw new SQLException("Cannot close result");
            }
            try {
                pstatement.close();
            } catch (Exception e1) {
                throw new SQLException("Cannot close statement");
            }
        }
        return state;
    }

    /**
     * 
     * Lets the teacher modify the grade of a certain student in the given exam session
     *
     */
    public void setResult(int SessionId, int StudentSerialNumber, String grade, int teacherId) throws SQLException{
        String query = "UPDATE examresult R, examsessions E, courses C SET R.Result = ?, R.State = 'INSERITO' WHERE R.SessionId = ? AND R.StudentSerialNumber = ? AND C.TeacherId = ? AND R.SessionId=E.SessionId AND C.CourseName=E.CourseName";
        PreparedStatement pstatement = null;
        pstatement = con.prepareStatement(query);
        pstatement.setString(1, grade.toUpperCase());
        pstatement.setString(2, String.valueOf(SessionId));
        pstatement.setString(3, String.valueOf(StudentSerialNumber));
        pstatement.setString(4, String.valueOf(teacherId));
        pstatement.executeUpdate();

    }

    /**
     * 
     * Sets the state from 'inserito' to 'pubblicato' in the given exam session 
     * 
     */
    public void publishExam(int SessionId, int teacherId) throws SQLException {
        String query = "UPDATE examresult R, examsessions E, courses C SET R.State = 'PUBBLICATO' WHERE R.SessionId = ? AND R.State = 'INSERITO' AND C.TeacherId = ? AND R.SessionId=E.SessionId AND C.CourseName=E.CourseName";
        PreparedStatement pstatement = null;
        pstatement = con.prepareStatement(query);
        pstatement.setString(1, String.valueOf(SessionId));
        pstatement.setString(2, String.valueOf(teacherId));
        pstatement.executeUpdate();
    }

    /**
     * 
     * Sets the state to 'rifiutato' in the given exam session
     * 
     */
    public void refuseExamResult(int SessionId, int StudentId) throws SQLException {
        String query = "UPDATE examresult R SET R.State = 'RIFIUTATO' WHERE R.SessionId = ? AND R.StudentSerialNumber = ?";
        PreparedStatement pstatement = null;
        pstatement = con.prepareStatement(query);
        pstatement.setString(1, String.valueOf(SessionId));
        pstatement.setString(2, String.valueOf(StudentId));
        pstatement.executeUpdate();
    }

    /**
     * 
     * Verbalizes the exam results, setting the result to 'rimandato' if the state is 'rifiutato'
     * 
     */
    public List<Integer> verbalizeExamResult(int SessionId, int teacherId) throws SQLException{
        ResultSet result = null;
        List<Integer> studentsList = new ArrayList<>();
        String studentsQuery = "SELECT R.StudentSerialNumber FROM examsessions E, examresult R, courses C WHERE R.SessionId = ? AND (R.State = 'PUBBLICATO' OR R.State = 'RIFIUTATO') AND C.TeacherId = ? AND R.SessionId=E.SessionId AND C.CourseName=E.CourseName";
        String query1 = "UPDATE examsessions E, examresult R, courses C SET R.State = 'VERBALIZZATO' WHERE R.SessionId = ? AND R.State = 'PUBBLICATO' AND C.TeacherId = ? AND R.SessionId=E.SessionId AND C.CourseName=E.CourseName";
        String query2 = "UPDATE examsessions E, examresult R, courses C SET R.State = 'VERBALIZZATO', R.Result = 'Rimandato' WHERE R.SessionId = ? AND R.State = 'RIFIUTATO' AND C.TeacherId = ? AND R.SessionId=E.SessionId AND C.CourseName=E.CourseName";
        PreparedStatement pstatementStudents = null;
        pstatementStudents = con.prepareStatement(studentsQuery);
        pstatementStudents.setString(1, String.valueOf(SessionId));
        pstatementStudents.setString(2, String.valueOf(teacherId));
        result = pstatementStudents.executeQuery();
        while (result.next()) {
            studentsList.add(result.getInt("StudentSerialNumber"));
        }
        con.setAutoCommit(false);
        PreparedStatement pstatement1 = null;
        PreparedStatement pstatement2 = null;
        try {
        	pstatement1 = con.prepareStatement(query1);
        	pstatement1.setString(1, String.valueOf(SessionId));
        	pstatement1.setString(2, String.valueOf(teacherId));
        	pstatement1.executeUpdate();
        	pstatement2 = con.prepareStatement(query2);
        	pstatement2.setString(1, String.valueOf(SessionId));
        	pstatement2.setString(2, String.valueOf(teacherId));
        	pstatement2.executeUpdate();
        	con.commit();
        } catch (SQLException e){
        	con.rollback();
        	throw e;
        } finally {
        	con.setAutoCommit(true);
            try {
                result.close();
            } catch (Exception e1) {
                throw new SQLException("Cannot close result");
            }
            try {
            	pstatementStudents.close();
                pstatement1.close();
                pstatement2.close();
            } catch (Exception e1) {
                throw new SQLException("Cannot close statement");
            }
        }
        return  studentsList;
    }

    /**
     * 
     * Given a session Id, shows all the students subscribed to that exam session with result state equals 'NON INSERITO'
     * 
     */
    public List<Student> findNotInsertedStudents(int sessionId, int teacherId) throws SQLException{
    	List<Student> students = new ArrayList<>();
    	String query = "SELECT S.SerialNumber, S.Surname, S.Name FROM students S, examsessions E, examresult R, courses C WHERE S.SerialNumber = R.StudentSerialNumber AND E.SessionId = ? AND C.TeacherId = ? AND R.State = 'NON INSERITO'"
    			+ "AND R.SessionId=E.SessionId AND C.CourseName=E.CourseName";
    	ResultSet result = null;
    	PreparedStatement pstatement = null;
    	try {
    		pstatement = con.prepareStatement(query);
            pstatement.setString(1, String.valueOf(sessionId));
            pstatement.setString(2, String.valueOf(teacherId));
            result = pstatement.executeQuery();
            while (result.next()) {
            	Student subscribedStudent = new Student();
            	subscribedStudent.setSerialNumber(result.getInt("SerialNumber"));
            	subscribedStudent.setSurname(result.getString("Surname"));
            	subscribedStudent.setName(result.getString("Name"));
            	students.add(subscribedStudent);
            }
    	} catch (SQLException e) {
            throw new SQLException(e);
        }finally {
            try {
                result.close();
            } catch (Exception e1) {
                throw new SQLException("Cannot close result");
            }
            try {
                pstatement.close();
            } catch (Exception e1) {
                throw new SQLException("Cannot close statement");
            }
        }
    	return students;
    }
    
    /**
     * 
     * Given a session Id, shows all the students subscribed to that exam session. tableOrder and ascending are used to reorder the table accordingly
     * 
     */
    public ExamSession findAllSubscribedStudents(int SessionID, String tableOrder, boolean ascending, int teacherId) throws SQLException{
        ExamSession examSession = new ExamSession();
        examSession.setSessionId(SessionID);
        String query = "SELECT S.SerialNumber, S.Surname, S.Name, S.Mail, S.DegreeCourse, R.Result, R.State " +
                "FROM students S, examsessions E, examresult R, courses C " +
                "WHERE S.SerialNumber = R.StudentSerialNumber AND R.SessionId = ? AND C.TeacherId = ? AND R.SessionId=E.SessionId AND C.CourseName=E.CourseName ";
        if (tableOrder == null) {
            tableOrder = "SerialNumber";
        }
        switch(tableOrder){
            case "SerialNumber":
                query = query + " ORDER BY S.SerialNumber";
                if(!ascending) query = query + " DESC";
                break;
            case "Surname":
                query = query + " ORDER BY S.Surname";
                if(!ascending) query = query + " DESC";
                break;
            case "Name":
                query =  query + SessionID + " ORDER BY S.Name";
                if(!ascending) query = query + " DESC";
                break;
            case "Mail":
                query = query + " ORDER BY S.Mail";
                if(!ascending) query = query + " DESC";
                break;
            case "DegreeCourse":
                query = query + " ORDER BY S.DegreeCourse";
                if(!ascending) query = query + " DESC";
                break;
            case "Result":
            	if(ascending) query = query + " ORDER BY IF(E.Result RLIKE '^[A-Z]', 1, 2), E.Result";
                else query = query + " ORDER BY IF(E.Result RLIKE '^[A-Z]', 1, 2) DESC, E.Result DESC";
                break;
            case "State":
                query = query + " ORDER BY E.State";
                if(!ascending) query = query + " DESC";
                break;
            default:
                break;
        }
        ResultSet result = null;
        PreparedStatement pstatement = null;
        try {
            pstatement = con.prepareStatement(query);
            pstatement.setString(1, String.valueOf(SessionID));
            pstatement.setString(2, String.valueOf(teacherId));
            result = pstatement.executeQuery();
            while (result.next()) {
                Student subscribedStudent = new Student();
                String sessionResult = result.getString("Result");
                String sessionState = result.getString("State");
                subscribedStudent.setSerialNumber(result.getInt("SerialNumber"));
                subscribedStudent.setSurname(result.getString("Surname"));
                subscribedStudent.setName(result.getString("Name"));
                subscribedStudent.setMail(result.getString("Mail"));
                subscribedStudent.setDegreeCourse(result.getString("DegreeCourse"));
                examSession.getSubscribers().add(subscribedStudent);
                examSession.getResults().add(sessionResult);
                examSession.getStates().add(sessionState);
            }
        } catch (SQLException e) {
            throw new SQLException(e);
        }finally {
            try {
                result.close();
            } catch (Exception e1) {
                throw new SQLException("Cannot close result");
            }
            try {
                pstatement.close();
            } catch (Exception e1) {
                throw new SQLException("Cannot close statement");
            }
        }
        return examSession;
    }
    
    /**
     * 
     * Used to check if the given teacher has an exam with the given id
     *
     */
    public boolean isSessionValid(int sessionId, int teacherId) throws SQLException{
    	boolean valid = false;
    	String query = "SELECT * FROM examsessions E, courses C  WHERE C.TeacherId = ? AND E.SessionId = ? AND C.CourseName=E.CourseName";
    	ResultSet result = null;
        PreparedStatement pstatement = null;
        try {
            pstatement = con.prepareStatement(query);
            pstatement.setString(1, String.valueOf(teacherId));
            pstatement.setString(2, String.valueOf(sessionId));
            result = pstatement.executeQuery();
            while(result.next()) {
            	valid = true;
            }
        } catch (SQLException e) {
            throw new SQLException(e);
        }finally {
            try {
                result.close();
            } catch (Exception e1) {
                throw new SQLException("Cannot close result");
            }
            try {
                pstatement.close();
            } catch (Exception e1) {
                throw new SQLException("Cannot close statement");
            }
        }
        return valid;
    }
    
    /**
     * 
     * Used to check if the given student is actually subscribed to the given exam session
     *
     */
    public boolean isStudentSubscribed(int sessionId, int studentId) throws SQLException{
    	boolean valid = false;
    	String query = "SELECT * FROM examresult R  WHERE R.StudentSerialNumber = ? AND R.SessionId = ?";
        ResultSet result = null;
        PreparedStatement pstatement = null;
        try {
            pstatement = con.prepareStatement(query);
            pstatement.setString(1, String.valueOf(studentId));
            pstatement.setString(2, String.valueOf(sessionId));
            result = pstatement.executeQuery();
            while(result.next()) {
            	valid = true;
            }
        } catch (SQLException e) {
            throw new SQLException(e);
        }finally {
            try {
                result.close();
            } catch (Exception e1) {
                throw new SQLException("Cannot close result");
            }
            try {
                pstatement.close();
            } catch (Exception e1) {
                throw new SQLException("Cannot close statement");
            }
        }
        return valid;
    }
}
