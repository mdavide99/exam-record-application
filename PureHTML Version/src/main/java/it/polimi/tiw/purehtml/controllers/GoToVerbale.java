package it.polimi.tiw.purehtml.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

import it.polimi.tiw.purehtml.beans.ExamSession;
import it.polimi.tiw.purehtml.beans.Student;
import it.polimi.tiw.purehtml.beans.Teacher;
import it.polimi.tiw.purehtml.dao.ExamSessionsDAO;
import it.polimi.tiw.purehtml.dao.ReportDAO;
import it.polimi.tiw.purehtml.dao.StudentsDAO;

/**
 * 
 * Shows a report each time a teacher verbalizes some grades
 *
 */
@WebServlet("/Verbale")
public class GoToVerbale extends HttpServlet {
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
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        StudentsDAO sDAO = new StudentsDAO(connection);
        ExamSessionsDAO eDAO = new ExamSessionsDAO(connection);
        ReportDAO rDAO = new ReportDAO(connection);

        HttpSession session = request.getSession();
        Teacher teacher = (Teacher) session.getAttribute("teacher");
        int examSessionId = 0;
        LocalDateTime now = LocalDateTime.now();//Get current date time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        String formatDateTime = now.format(formatter);
        int reportId = 0;
        ExamSession examSession = null;
        List <Integer> studentsId= null;
        List <Student> students = new ArrayList<>();
        List <String> results = new ArrayList<>();

        try {
            examSessionId = Integer.parseInt(request.getParameter("examSessionID"));
            examSession = eDAO.getExamSession(examSessionId);
            if(!eDAO.isSessionValid(examSessionId, teacher.getTeacherId())) {
            	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            	response.getWriter().println("Sessione non trovata");
            	return;
            }
            studentsId = eDAO.verbalizeExamResult(examSessionId, teacher.getTeacherId());
            for (Integer studentId:studentsId) {
                students.add(sDAO.getStudent(studentId));
            }
            for (Student student:students) {
                results.add(eDAO.getResult(examSessionId,student.getSerialNumber()));
            }
            reportId = rDAO.newReportId();

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String path = "Templates/Verbale.html";
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        ctx.setVariable("reportId", reportId);
        ctx.setVariable("creationData", formatDateTime);
        ctx.setVariable("examSession", examSession);
        ctx.setVariable("students", students);
        ctx.setVariable("results", results);
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
