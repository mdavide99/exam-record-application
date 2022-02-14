package it.polimi.tiw.purehtml.controllers;

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

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.purehtml.beans.Course;
import it.polimi.tiw.purehtml.beans.ExamSession;
import it.polimi.tiw.purehtml.beans.Student;
import it.polimi.tiw.purehtml.dao.CourseDAO;
import it.polimi.tiw.purehtml.dao.StudentsDAO;

/**
 * 
 * Handles the home page for students, showing all their courses and the respective exam session dates.
 *
 */
@WebServlet("/HomeStudent")
public class GoToHomeStudent extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;
    private TemplateEngine templateEngine;

    public void init() throws ServletException {
        ServletContext servletContext = getServletContext();
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");

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
            throwables.printStackTrace();
        }
        for (Course currentCourse : courses) {
            List<ExamSession> examSessionList = null;
            try {
                examSessionList = cDAO.findExamDatesStudent(currentCourse.getCourseName(), student.getSerialNumber());
                currentCourse.setExamSessionList(examSessionList);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        String path = "Templates/HomeStudent.html";
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        ctx.setVariable("courses", courses);
        templateEngine.process(path, ctx, response.getWriter());
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
