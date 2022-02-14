package it.polimi.tiw.withJavaScript.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

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
import it.polimi.tiw.withJavaScript.beans.ExamResult;
import it.polimi.tiw.withJavaScript.beans.Student;
import it.polimi.tiw.withJavaScript.dao.ExamSessionsDAO;

/**
 * 
 * Serializes all the informations regarding multiple results for an exam session
 *
 */
@WebServlet("/SetBulkOfResult")
@MultipartConfig
public class SetBulkOfResults extends HttpServlet{
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
        List<Student> students = null;

        try{
            examSessionId = Integer.parseInt(request.getParameter("examSessionID"));
                        
            if(examSessionsDAO.isSessionValid(examSessionId, teacher.getTeacherId())) {
            students = examSessionsDAO.findNotInsertedStudents(examSessionId, teacher.getTeacherId());
            }
            
            if(students == null || students.size() == 0) {
            	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            	response.getWriter().println("Nessuna Sezione Corrispondente");
            	return;
            }
            
            
        } catch (NumberFormatException e) {
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        	response.getWriter().println("Errore nella generazione della richiesta");
            return;
        }catch (SQLException throwables) {
        	response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Wrong Query!");
        	response.getWriter().println("Errore interno");
        	return;
        }
        Gson gson = new Gson();
        String json = gson.toJson(students);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	 int examSessionId= 0;
        HttpSession session = request.getSession();
        Teacher teacher = (Teacher) session.getAttribute("teacher");
        ExamSessionsDAO examSessionsDAO = new ExamSessionsDAO(connection);
        List<Student> students = null;
        Gson gson = new Gson();
        
        try{
            examSessionId = Integer.parseInt(request.getParameter("examSessionID"));
                        
            if(examSessionsDAO.isSessionValid(examSessionId, teacher.getTeacherId())) {
            students = examSessionsDAO.findNotInsertedStudents(examSessionId, teacher.getTeacherId());
            }
            
            if(students == null || students.size() == 0) {
            	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            	response.getWriter().println("Nessuna Sezione Corrispondente");
            	return;
            }
            
        } catch (NumberFormatException e) {
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        	response.getWriter().println("Errore nella generazione della richiesta");
            return;
        }catch (SQLException throwables) {
        	response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Wrong Query!");
        	response.getWriter().println("Errore interno");
        	return;
        }
        
        try {
            StringBuilder sb = new StringBuilder();
            String s;
            while ((s = request.getReader().readLine()) != null) {
                sb.append(s);
            }
            ExamResult[] examResults = gson.fromJson(sb.toString(),ExamResult[].class);
            for(ExamResult examResult : examResults) {
            		String state = examSessionsDAO.getState(examSessionId, examResult.getStudent().getSerialNumber());
                    if (isResultEditable(state)){
                    	examSessionsDAO.setResult(examSessionId, examResult.getStudent().getSerialNumber(), examResult.getResult(), teacher.getTeacherId());
                    }
            }
        }catch (Exception ex) {
        	response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Wrong Query!");
        	response.getWriter().println("Errore interno");
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