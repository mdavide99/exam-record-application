package it.polimi.tiw.withJavaScript.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.withJavaScript.beans.Course;
import it.polimi.tiw.withJavaScript.beans.Teacher;

/**
 * 
 * DAO used to handle interactions with the database regarding teachers
 *
 */
public class TeachersDAO {
    private Connection con;

    public TeachersDAO(Connection con) {
        this.con = con;
    }

    /**
     * 
     * Selects the correct teacher after the login session
     * 
     */
    public Teacher teacherLogin(String username, String password) throws SQLException {
        String query = "SELECT TeacherId FROM teachers WHERE mail = ? AND password = ?";
        try (PreparedStatement pstatement = con.prepareStatement(query)) {
            pstatement.setString(1, username);
            pstatement.setString(2, password);

            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) {
                    return null; // no user password match
                } else {
                    result.next();
                    Teacher teacher = new Teacher();
                    teacher.setTeacherId(result.getInt("TeacherId"));
                    return teacher;
                }
            }
        }
    }

    /**
     * 
     * Returns a list of all the courses taught by the given teacher
     * 
     */
    public List<Course> findTeacherCourses(int teacherId) throws SQLException {
        List<Course> courses = new ArrayList<>();
        String query = "SELECT C.CourseName " +
                "FROM courses C  " +
                "WHERE C.TeacherId = ?" +
                " ORDER BY CourseName desc";
        ResultSet result = null;
        PreparedStatement pstatement = null;
        try {
            pstatement = con.prepareStatement(query);
            pstatement.setString(1, String.valueOf(teacherId));
            result = pstatement.executeQuery();
            while (result.next()) {
                Course course = new Course();
                course.setCourseName(result.getString("CourseName"));
                courses.add(course);
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
        return courses;
    }
    
    /**
     * 
     * Returns the salt used in the database to obfuscate the teacher's password
     * 
     */
    public String getTeacherSalt(String mail) throws SQLException {
        String out = null;
        String query = "SELECT Salt FROM teachers WHERE mail = ?";
        ResultSet result = null;
        PreparedStatement pstatement = null;
        try {
            pstatement = con.prepareStatement(query);
            pstatement.setString(1,mail);
            result = pstatement.executeQuery();
            while (result.next()) {
                out = result.getString("Salt");
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
        return out;
    }
}
