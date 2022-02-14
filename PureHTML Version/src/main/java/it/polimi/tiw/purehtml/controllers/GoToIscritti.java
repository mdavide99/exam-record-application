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
import it.polimi.tiw.purehtml.beans.Teacher;
import it.polimi.tiw.purehtml.dao.ExamSessionsDAO;

/**
 * 
 * Handles the page that shows all the students subscribed to a certain exam session
 *
 */
@WebServlet("/Iscritti")
public class GoToIscritti extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;
    private TemplateEngine templateEngine;
    private boolean ascendingSerialNumber = true;
    private boolean ascendingSurname = true;
    private boolean ascendingMail = true;
    private boolean ascendingDegreeCourse = true;
    private boolean ascendingResult = true;
    private boolean ascendingState = true;

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
        int examSessionId= 0;
        String tableOrder = null;
        boolean ascending = true;
        HttpSession session = request.getSession();
        Teacher teacher = (Teacher) session.getAttribute("teacher");
        ExamSessionsDAO examSessionsDAO = new ExamSessionsDAO(connection);
        ExamSession examSession = null;

        try{
            examSessionId = Integer.parseInt(request.getParameter("examSessionID"));
            tableOrder = request.getParameter("tableOrder");
            ascending = Boolean.parseBoolean(request.getParameter("ascending"));
            examSession = examSessionsDAO.findAllSubscribedStudents(examSessionId, tableOrder, ascending, teacher.getTeacherId());
            if(  examSession == null || !examSessionsDAO.isSessionValid(examSessionId, teacher.getTeacherId())) {
            	response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Trying to access another teacher's data");
            	return;
            }
            examSession.setSessionId(examSessionId);
            resetAscending();
            setAscending(tableOrder,ascending);
        } catch (NumberFormatException | SQLException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } 

        String path = "Templates/Iscritti.html";
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        ctx.setVariable("examSession", examSession);
        ctx.setVariable("ascendingSerialNumber",ascendingSerialNumber);
        ctx.setVariable("ascendingSurname",ascendingSurname);
        ctx.setVariable("ascendingMail",ascendingMail);
        ctx.setVariable("ascendingDegreeCourse",ascendingDegreeCourse);
        ctx.setVariable("ascendingResult",ascendingResult);
        ctx.setVariable("ascendingState",ascendingState);
        templateEngine.process(path, ctx, response.getWriter());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ExamSessionsDAO eDAO = new ExamSessionsDAO(connection);
        HttpSession session = request.getSession();
        Teacher teacher = (Teacher) session.getAttribute("teacher");
        int examSessionId = Integer.parseInt(request.getParameter("examSessionID"));
        try {
            eDAO.publishExam(examSessionId, teacher.getTeacherId());
            
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        String path = getServletContext().getContextPath();
        String target = "/Iscritti?examSessionID="+examSessionId+"&tableOrder=SerialNumber&ascending=true";
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
     * Reorders the table accordingly to the selected parameter
     *
     */
    private void setAscending(String tableOrder, boolean current){
        current = !current;
        if (tableOrder == null) {
            tableOrder="SerialNumber";
        }
        switch(tableOrder){
            case "SerialNumber":
                ascendingSerialNumber = current;
                break;
            case "Surname":
                ascendingSurname = current;
                break;
            case "Mail":
                ascendingMail = current;
                break;
            case "DegreeCourse":
                ascendingDegreeCourse = current;
                break;
            case "Result":
                ascendingResult = current;
                break;
            case "State":
                ascendingState = current;
                break;
            default:
                break;
        }
    }

    /**
     * Used to reset the table order, helping to set the correct order each time a parameter is selected
     */
    private void resetAscending(){
        ascendingSerialNumber = true;
        ascendingSurname = true;
        ascendingMail = true;
        ascendingDegreeCourse = true;
        ascendingResult = true;
        ascendingState = true;
    }

}
