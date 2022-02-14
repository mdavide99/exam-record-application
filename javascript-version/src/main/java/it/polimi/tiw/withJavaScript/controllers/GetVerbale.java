package it.polimi.tiw.withJavaScript.controllers;

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
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import it.polimi.tiw.withJavaScript.beans.Teacher;
import it.polimi.tiw.withJavaScript.beans.ExamSession;
import it.polimi.tiw.withJavaScript.beans.Student;
import it.polimi.tiw.withJavaScript.beans.Verbale;
import it.polimi.tiw.withJavaScript.dao.ExamSessionsDAO;
import it.polimi.tiw.withJavaScript.dao.ReportDAO;
import it.polimi.tiw.withJavaScript.dao.StudentsDAO;

/**
 * 
 * Serialize all informations regarding a report each time a teacher verbalizes some grades
 *
 */
@WebServlet("/Verbale")
@MultipartConfig
public class GetVerbale extends HttpServlet {
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
        StudentsDAO sDAO = new StudentsDAO(connection);
        ExamSessionsDAO eDAO = new ExamSessionsDAO(connection);
        ReportDAO rDAO = new ReportDAO(connection);

        HttpSession session = request.getSession();
        Teacher teacher = (Teacher) session.getAttribute("teacher");
        int examSessionId = 0;
        LocalDateTime now = LocalDateTime.now();//Get current date time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        String formatDateTime = now.format(formatter);
        Verbale report = new Verbale();
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
            report.setExamSession(examSession);
            for (Integer studentId:studentsId) {
                students.add(sDAO.getStudent(studentId));
            }
            for (Student student:students) {
                results.add(eDAO.getResult(examSessionId,student.getSerialNumber()));
            }
            report.setStudents(students);
            report.setResults(results);
            reportId = rDAO.newReportId();
            report.setReportId(reportId);
            report.setDateTime(formatDateTime);

        } catch (NumberFormatException | SQLException e ) {
        	response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        	response.getWriter().println("Sessione non trovata");
            return;
        }
        Gson gson = new Gson();
        String json = gson.toJson(report);
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
