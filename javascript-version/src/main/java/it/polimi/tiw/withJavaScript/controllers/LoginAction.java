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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang.StringEscapeUtils;

import it.polimi.tiw.withJavaScript.beans.Student;
import it.polimi.tiw.withJavaScript.beans.Teacher;
import it.polimi.tiw.withJavaScript.dao.StudentsDAO;
import it.polimi.tiw.withJavaScript.dao.TeachersDAO;

/**
 * 
 * Serializes all the informations needed for the Login Action
 *
 */
@WebServlet("/LoginAction")
@MultipartConfig
public class LoginAction extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public LoginAction() {
        super();
    }

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
        response.getWriter().append("Served at: ").append(request.getContextPath());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	
    	String mail = null;
    	String password = null;
    	String salt = null;
    	String hashPwd = null;
        mail = StringEscapeUtils.escapeJava(request.getParameter("mail"));
        password = StringEscapeUtils.escapeJava(request.getParameter("password"));
        if (mail == null ||mail.isEmpty() ||password == null ||password.isEmpty()) {
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        	response.getWriter().println("Credenziali nulle");
        	return;
        }
        
        TeachersDAO teachersDAO = new TeachersDAO(connection);
        StudentsDAO studentsDAO = new StudentsDAO(connection);
        Teacher teacher = null;
        Student student = null;
        try {
        	salt = teachersDAO.getTeacherSalt(mail);
        	if (salt != null) {
	        	hashPwd = hashPassword( password, salt);
	            teacher = teachersDAO.teacherLogin(mail,hashPwd);
        	}
            if (teacher == null) {
            	salt = studentsDAO.getTeacherSalt(mail);
            	if (salt != null) {
    	        	hashPwd = hashPassword( password, salt);
    	        	student = studentsDAO.studentLogin(mail,hashPwd);
            	}
            }
        } catch (SQLException | NoSuchAlgorithmException e) {
            response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
        	response.getWriter().println("Errore Interno");
        	return;
        }

        if (teacher==null && student == null){
        	response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        	response.getWriter().println("Credenziali non valide");
        }else if (teacher != null){
            request.getSession().setAttribute("teacher", teacher);
        	response.setStatus(HttpServletResponse.SC_OK);
        	response.setContentType("application/json");
        	response.setCharacterEncoding("UTF-8");
        	response.getWriter().println("teacher:"+ teacher.getTeacherId());

        }else {
        	request.getSession().setAttribute("student", student);
        	response.setStatus(HttpServletResponse.SC_OK);
        	response.setContentType("application/json");
        	response.setCharacterEncoding("UTF-8");
        	response.getWriter().println("student:"+ student.getSerialNumber());
        }

    }

    public void destroy() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ignored) {
        }
    }
    
    public String hashPassword(String password,String salt)
    		 throws NoSuchAlgorithmException {
    	 password = password + salt;
    	 MessageDigest md = MessageDigest.getInstance("SHA-256");
    	 md.update(password.getBytes());
    	 byte[] mdArray = md.digest();
    	 StringBuilder sb = new StringBuilder(mdArray.length * 2);
    	 for (byte b : mdArray) {
	    	 int v = b & 0xff;
	    	 if (v < 16) {
	    		 sb.append('0');
	    	 }
	    	 sb.append(Integer.toHexString(v));
	    	 }
	    	 return sb.toString();
    	 }
    
    
}
