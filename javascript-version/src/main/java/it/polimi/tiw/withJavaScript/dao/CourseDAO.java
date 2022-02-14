package it.polimi.tiw.withJavaScript.dao;

import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.withJavaScript.beans.ExamSession;

/**
 * 
 * DAO used to handle interactions with the database regarding courses
 *
 */
public class CourseDAO {
    private Connection con;

    public CourseDAO(Connection con) {
        this.con = con;
    }

    /**
     * 
     * Returns a list of possible exam sessions for the given course
     * 
     */
    public List<ExamSession> findExamDates(String courseName) throws SQLException {
        List<ExamSession> examSessionList = new ArrayList<>();
        String query = "SELECT DISTINCT E.SessionDate, E.SessionId " +
                "FROM courses C, examsessions E " +
                "WHERE C.CourseName = E.CourseName and E.CourseName = ? ORDER BY E.SessionDate desc";
        ResultSet result = null;
        PreparedStatement pstatement = null;
        try {
            pstatement = con.prepareStatement(query);
            pstatement.setString(1,  courseName);
            result = pstatement.executeQuery();
            while (result.next()) {
                ExamSession examSession = new ExamSession();
                examSession.setSessionDate(result.getString("SessionDate"));
                examSession.setSessionId(result.getInt("SessionId"));
                examSessionList.add(examSession);
            }
        } catch (SQLException e) {
            throw new SQLException(e);
        } finally {
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
        return examSessionList;
    }

    /**
     * 
     * Returns a list of all the exam session a given student is subscribed to
     * 
     */
    public List<ExamSession> findExamDatesStudent(String courseName, int studentId) throws SQLException {
        List<ExamSession> examSessionList = new ArrayList<>();
        String query = "SELECT DISTINCT E.SessionDate, E.SessionId " +
                "FROM courses C, examsessions E, examresult R " +
                "WHERE C.CourseName = E.CourseName and E.CourseName = ? AND R.SessionId=E.SessionId AND R.StudentSerialNumber=? ORDER BY E.SessionDate desc";
        ResultSet result = null;
        PreparedStatement pstatement = null;
        try {
            pstatement = con.prepareStatement(query);
            pstatement.setString(1,  courseName);
            pstatement.setString(2,  String.valueOf(studentId));
            result = pstatement.executeQuery();
            while (result.next()) {
                ExamSession examSession = new ExamSession();
                examSession.setSessionDate(result.getString("SessionDate"));
                examSession.setSessionId(result.getInt("SessionId"));
                examSessionList.add(examSession);
            }
        } catch (SQLException e) {
            throw new SQLException(e);
        } finally {
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
        return examSessionList;
    }
}
