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

import it.polimi.tiw.withJavaScript.beans.ExamResult;
import it.polimi.tiw.withJavaScript.beans.Student;
import it.polimi.tiw.withJavaScript.dao.ExamSessionsDAO;
import it.polimi.tiw.withJavaScript.dao.StudentsDAO;

/**
 * 
 * Serializes all the informations regarding a student's result in a given exam session
 *
 */
@WebServlet("/GetExamResult")
@MultipartConfig
public class GetExamResult extends HttpServlet {
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
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        StudentsDAO sDAO = new StudentsDAO(connection);
        ExamSessionsDAO eDAO = new ExamSessionsDAO(connection);
        ExamResult examResult = new ExamResult();
        int examSessionId = 0;
        String result = null;
        String state = null;
        Student student = null;

        try {
            student = (Student) session.getAttribute("student");
            student = sDAO.getStudent(student.getSerialNumber());
            examResult.setStudent(student);
            examSessionId = Integer.parseInt(request.getParameter("examSessionID"));
            if(!eDAO.isStudentSubscribed(examSessionId, student.getSerialNumber())) {
            	response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            	response.getWriter().println("Nessuna Sezione Corrispondente");
            	return;
            }
            examResult.setExamSession(eDAO.getExamSession(examSessionId));
            result = eDAO.getResult(examSessionId, student.getSerialNumber());
            state = eDAO.getState(examSessionId, student.getSerialNumber());
            examResult.setResult(result);
            examResult.setState(state);
            examResult.setRefusable(isResultRefusable(result, state));
            
        } catch (NumberFormatException e) {
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        	response.getWriter().println("Errore nel invio della richiesta");
        	return;
        }catch (SQLException e) {
            e.printStackTrace();
        }
        Gson gson = new Gson();
        String json = gson.toJson(examResult);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
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
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        	response.getWriter().println("Nessuna Sezione Corrispondente");
        	return;
        }
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("OK");
    }

    public void destroy() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ignored) {
        }
    }

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
