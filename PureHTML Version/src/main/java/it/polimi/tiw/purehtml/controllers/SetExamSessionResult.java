package it.polimi.tiw.purehtml.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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

import it.polimi.tiw.purehtml.beans.Student;
import it.polimi.tiw.purehtml.beans.Teacher;
import it.polimi.tiw.purehtml.dao.ExamSessionsDAO;
import it.polimi.tiw.purehtml.dao.StudentsDAO;

/**
 * 
 * Handles the modification of exam results by the teacher
 *
 */
@WebServlet("/ExamSessionResult")
public class SetExamSessionResult extends HttpServlet {
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
        Integer examSessionId = null;
        Student student = null;
        StudentsDAO sDAO = new StudentsDAO(connection);
        ExamSessionsDAO eDAO = new ExamSessionsDAO(connection);
        HttpSession session = request.getSession();
        Teacher teacher = (Teacher) session.getAttribute("teacher");

        try {
            examSessionId = Integer.parseInt(request.getParameter("examSessionID"));
            int studentId = Integer.parseInt(request.getParameter("studentId"));
            student = sDAO.getStudent(studentId);
            if(!eDAO.isSessionValid(examSessionId, teacher.getTeacherId()) || !eDAO.isStudentSubscribed(examSessionId, studentId)) {
            	response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            	return;
            }
            eDAO.getResult(examSessionId, studentId);
            eDAO.getState(examSessionId,studentId);
        } catch (NumberFormatException | SQLException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String path = "Templates/ExamSessionResult.html";
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        ctx.setVariable("student", student);
        ctx.setVariable("examSessionId", examSessionId);
        templateEngine.process(path, ctx, response.getWriter());

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	HttpSession session = request.getSession();
        Teacher teacher = (Teacher) session.getAttribute("teacher");
        ExamSessionsDAO eDAO = new ExamSessionsDAO(connection);
        String result = request.getParameter("result");
        List<String> possibleResults = new ArrayList(){{
         	add("Rimandato");
         	add("Riprovato");
         	add("Assente");
         	add("18");
         	add("19");
         	add("20");
         	add("21");
         	add("22");
         	add("23");
         	add("24");
         	add("25");
         	add("26");
         	add("27");
         	add("28");
         	add("29");
         	add("30");
         	add("30 e Lode");
        }};
        if(!possibleResults.contains(result)) {
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        int examSessionId = Integer.parseInt(request.getParameter("examSessionID"));
        int studentId = Integer.parseInt(request.getParameter("studentId"));
        try {
            String state = eDAO.getState(examSessionId, studentId);
            if (isResultEditable(state)){
                eDAO.setResult(examSessionId, studentId, result, teacher.getTeacherId());
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        String path = getServletContext().getContextPath();
        String target = "/Iscritti?examSessionID=" + examSessionId + "&tableOrder=SerialNumber&ascending=true";
        path = path + target;
        response.sendRedirect(path);
    }

    public void destroy() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ignored) {
        }
    }

    /**
     * 
     * Checks if the teacher can still edit the result of the exam
     * 
     */
    private boolean isResultEditable(String resultStatus) {
        if (resultStatus == null) {
            return false;
        }
        resultStatus=resultStatus.toLowerCase();
        switch (resultStatus) {
            case ("non inserito"):
            case ("inserito"):
                return true;

            default:
                return false;
        }
    }
}
