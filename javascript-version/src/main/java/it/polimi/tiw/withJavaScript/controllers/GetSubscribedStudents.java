package it.polimi.tiw.withJavaScript.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import it.polimi.tiw.withJavaScript.beans.Teacher;
import it.polimi.tiw.withJavaScript.beans.ExamSession;
import it.polimi.tiw.withJavaScript.dao.ExamSessionsDAO;

/**
 * 
 * Serialize all informations regarding all the students subscribed to a certain exam session
 *
 */
@WebServlet("/GetSubscribedStudents")
@MultipartConfig
public class GetSubscribedStudents extends HttpServlet {
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
        int examSessionId= 0;
        HttpSession session = request.getSession();
        Teacher teacher = (Teacher) session.getAttribute("teacher");
        ExamSessionsDAO examSessionsDAO = new ExamSessionsDAO(connection);
        ExamSession examSession = null;

        try{
            examSessionId = Integer.parseInt(request.getParameter("examSessionID"));
                        
            if(examSessionsDAO.isSessionValid(examSessionId, teacher.getTeacherId())) {
            examSession = examSessionsDAO.findAllSubscribedStudents(examSessionId,null, true, teacher.getTeacherId());
            }
            
            if(examSession == null || examSession.getSubscribers().size() == 0) {
            	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            	response.getWriter().println("Nessuna Sezione Corrispondente");
            	return;
            }
            
        } catch (NumberFormatException e) {
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        	response.getWriter().println("Errore nella generazione della richiesta");
            return;
        }catch (SQLException throwables) {
        	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        	response.getWriter().println("Errore interno");
        	return;
        }
        Gson gson = new Gson();
        String json = gson.toJson(examSession);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
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
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        	response.getWriter().println("Errore nella generazione della richiesta");
            return;
        }
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().println("OK");
        
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
