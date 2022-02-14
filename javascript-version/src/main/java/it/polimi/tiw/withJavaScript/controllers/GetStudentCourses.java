package it.polimi.tiw.withJavaScript.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import it.polimi.tiw.withJavaScript.beans.Course;
import it.polimi.tiw.withJavaScript.beans.ExamSession;
import it.polimi.tiw.withJavaScript.beans.Student;
import it.polimi.tiw.withJavaScript.dao.CourseDAO;
import it.polimi.tiw.withJavaScript.dao.StudentsDAO;
/**
 * 
 * Serializes all the informations regarding all the courses a student is subscribed to
 *
 */
@WebServlet("/GetStudentCourses")
public class GetStudentCourses extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public void init() throws ServletException {

        try {
            ServletContext context = getServletContext();
            String driver = context.getInitParameter("dbDriver");
            String url = context.getInitParameter("dbUrl");
            String user = context.getInitParameter("dbUser");
            String password = context.getInitParameter("dbPassword");
            Class.forName(driver);
            connection = DriverManager.getConnection(url, user, password);
        } catch ( SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Student student = (Student) session.getAttribute("student");
        StudentsDAO sDAO = new StudentsDAO(connection);
        CourseDAO cDAO = new CourseDAO(connection);
        List<Course> courses = null;
        try {
            courses = sDAO.findStudentCourses(student.getSerialNumber());
        } catch (SQLException throwables) {
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        	response.getWriter().println("Nessuna Corso Corrispondente");
        	return;
        }
        for (Course currentCourse : courses) {
            List<ExamSession> examSessionList = null;
            try {
                examSessionList = cDAO.findExamDatesStudent(currentCourse.getCourseName(), student.getSerialNumber());
                currentCourse.setExamSessionList(examSessionList);
            } catch (SQLException e) {
            	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            	response.getWriter().println("Errore Interno");
            	return;
            }
        }
        Gson gson = new Gson();
        String json = gson.toJson(courses);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    public void destroy() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ignored) {
        }
    }
}
