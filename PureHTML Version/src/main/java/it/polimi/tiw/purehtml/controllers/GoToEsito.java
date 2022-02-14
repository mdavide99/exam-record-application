package it.polimi.tiw.purehtml.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
import it.polimi.tiw.purehtml.dao.ExamSessionsDAO;
import it.polimi.tiw.purehtml.dao.StudentsDAO;

/**
 * 
 * Controller that handles the interactions between the student and their result in a given exam session
 *
 */
@WebServlet("/Esito")
public class GoToEsito extends HttpServlet {
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
        HttpSession session = request.getSession();
        StudentsDAO sDAO = new StudentsDAO(connection);
        ExamSessionsDAO eDAO = new ExamSessionsDAO(connection);
        int examSessionId = 0;
        String result = null;
        String state = null;
        Student student = null;
        ExamSession examSession = null;

        try {
            student = (Student) session.getAttribute("student");
            student = sDAO.getStudent(student.getSerialNumber());
            examSessionId = Integer.parseInt(request.getParameter("examSessionID"));
            if(!eDAO.isStudentSubscribed(examSessionId, student.getSerialNumber())) {
            	response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            	return;
            }
            examSession = eDAO.getExamSession(examSessionId);
            result = eDAO.getResult(examSessionId, student.getSerialNumber());
            state = eDAO.getState(examSessionId, student.getSerialNumber());

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (SQLException e) {
            e.printStackTrace();
        }


        String path = "Templates/Esito.html";
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        ctx.setVariable("student", student);
        ctx.setVariable("result", result);
        ctx.setVariable("state", state.toLowerCase());
        ctx.setVariable("resultRefusable", isResultRefusable(result, state));
        ctx.setVariable("examSession", examSession);
        templateEngine.process(path, ctx, response.getWriter());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        ExamSessionsDAO eDAO = new ExamSessionsDAO(connection);
        int examSessionId = 0;
        boolean refuse = false;
        Student student = (Student) session.getAttribute("student");
        try {
            examSessionId = Integer.parseInt(request.getParameter("examSessionID"));
            refuse= Boolean.parseBoolean(request.getParameter("refuse"));
            if (refuse){
                eDAO.refuseExamResult(examSessionId,student.getSerialNumber());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String path = getServletContext().getContextPath();
        String target = "/Esito?examSessionID="+examSessionId;
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
     * Checks that the result is refusable by the student
     * 
     */
    private boolean isResultRefusable( String result, String state ){
        if (result == null || state == null) {
            return false;
        }
        state = state.toLowerCase();
        if (state.equals("verbalizzato")) return false;
        result=result.toLowerCase();
        switch (result){
            case ("riprovato"):
            case ("rimandato"):
            case ("assente"):
                return false;
            default:
                return true;
        }
    }
}
