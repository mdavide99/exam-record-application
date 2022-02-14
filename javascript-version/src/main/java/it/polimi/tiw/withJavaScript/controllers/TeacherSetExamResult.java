package it.polimi.tiw.withJavaScript.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
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
import it.polimi.tiw.withJavaScript.dao.StudentsDAO;

/**
 * 
 * Handles the modification of exam results by the teacher
 *
 */
@WebServlet("/SetExamResult")
@MultipartConfig
public class TeacherSetExamResult extends HttpServlet {
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
        Integer examSessionId = null;
        Student student = null;
        ExamResult examResult = new ExamResult();
        StudentsDAO sDAO = new StudentsDAO(connection);
        ExamSessionsDAO eDAO = new ExamSessionsDAO(connection);
        HttpSession session = request.getSession();
        Teacher teacher = (Teacher) session.getAttribute("teacher");
        String result = null;
        String state = null;

        try {
            examSessionId = Integer.parseInt(request.getParameter("examSessionID"));
            int studentId = Integer.parseInt(request.getParameter("studentId"));
            student = sDAO.getStudent(studentId);
            result = eDAO.getResult(examSessionId, studentId);
            state = eDAO.getState(examSessionId,studentId);
            if(!eDAO.isSessionValid(examSessionId, teacher.getTeacherId()) || !eDAO.isStudentSubscribed(examSessionId, studentId)) {
            	response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            	return;
            }
            examResult.setStudent(student);
            examResult.setState(state);
            examResult.setResult(result);
        } catch (NumberFormatException | SQLException e) {
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        	response.getWriter().println("Nessuna Sezione Corrispondente");
        	return;
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
        Teacher teacher = (Teacher) session.getAttribute("teacher");
        ExamSessionsDAO eDAO = new ExamSessionsDAO(connection);
        String result = request.getParameter("result");
        //String[] possibleResults = {"Rimandato", "Riprovato", "Assente", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "30 e Lode"};
        List<String> possibleResults = new ArrayList<String>(){/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

		{
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
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        	response.getWriter().println("Errore, impossibile impostare la valutazione perch� non conforme");
            return;
        }
        int examSessionId = Integer.parseInt(request.getParameter("setResultSessionID"));
        int studentId = Integer.parseInt(request.getParameter("setResultStudentID"));
        try {
            String state = eDAO.getState(examSessionId, studentId);
            if (isResultEditable(state)){
                eDAO.setResult(examSessionId, studentId, result, teacher.getTeacherId());
            }

        } catch (SQLException throwables) {
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        	response.getWriter().println("La Sezioneo gli studenti selezionati non sono validi");
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
