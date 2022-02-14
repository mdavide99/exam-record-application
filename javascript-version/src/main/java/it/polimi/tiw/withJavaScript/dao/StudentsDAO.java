package it.polimi.tiw.withJavaScript.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.withJavaScript.beans.Course;
import it.polimi.tiw.withJavaScript.beans.Student;

/**
 * 
 * DAO used to handle interactions with the database regarding students
 *
 */
public class StudentsDAO {
    private Connection con;

    public StudentsDAO(Connection con) {
        this.con = con;
    }

    /**
     * 
     * Gets all the information of the given student
     * 
     */
    public Student getStudent(int serialNumber) throws  SQLException{
        Student student = new Student();
        String query = "SELECT S.Name,S.Surname,S.Mail,S.DegreeCourse FROM students S WHERE S.SerialNumber = ?";
        ResultSet result = null;
        PreparedStatement pstatement = null;
        try {
            pstatement = con.prepareStatement(query);
            pstatement.setString(1, String.valueOf(serialNumber));
            result = pstatement.executeQuery();
            if (!result.isBeforeFirst()) {
                return null; // student no found
            }else{
                result.next();
                student.setSerialNumber(serialNumber);
                student.setSerialNumber(serialNumber);
                student.setName(result.getString("Name"));
                student.setSurname(result.getString("Surname"));
                student.setMail(result.getString("Mail"));
                student.setDegreeCourse(result.getString("DegreeCourse"));
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
        return student;
    }


    /**
     * 
     * Returns a list with all the courses a student is subscribed to
     * 
     */
    public List<Course> findStudentCourses(int studentId) throws SQLException {
        List<Course> courses = new ArrayList<>();
        String query = "SELECT C.CourseName FROM studentcourses C  WHERE C.StudentSerialNumber = ? ORDER BY CourseName desc";
        ResultSet result = null;
        PreparedStatement pstatement = null;
        try {
            pstatement = con.prepareStatement(query);
            pstatement.setString(1, String.valueOf(studentId));
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
     * Returns the correct student after the login phase
     *
     */
    public Student studentLogin(String username, String password) throws SQLException {
        String query = "SELECT SerialNumber FROM students WHERE mail = ? AND password = ?";
        try (PreparedStatement pstatement = con.prepareStatement(query)) {
            pstatement.setString(1, username);
            pstatement.setString(2, password);

            try (ResultSet result = pstatement.executeQuery()) {
                if (!result.isBeforeFirst()) {
                    return null; // no user password match
                } else {
                    result.next();
                    Student student = new Student();
                    student.setSerialNumber(result.getInt("SerialNumber"));
                    return student;
                }
            }
        }
    }
    
    /**
     * 
     * Returns the salt used in the database to obfuscate the student's password
     * 
     */
    public String getTeacherSalt(String mail) throws SQLException {
        String out = null;
        String query = "SELECT Salt FROM students WHERE mail = ?";
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
