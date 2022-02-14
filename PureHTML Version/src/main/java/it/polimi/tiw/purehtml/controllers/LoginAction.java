package it.polimi.tiw.purehtml.controllers;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import it.polimi.tiw.purehtml.beans.Student;
import it.polimi.tiw.purehtml.beans.Teacher;
import it.polimi.tiw.purehtml.dao.StudentsDAO;
import it.polimi.tiw.purehtml.dao.TeachersDAO;

/**
 * 
 * Handles the login action, providing some extra control to the credentials
 *
 */
@WebServlet("/LoginAction")
public class LoginAction extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public LoginAction() {
        super();
        // TODO Auto-generated constructor stub
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
        String mail = request.getParameter("mail");
        String password = request.getParameter("password");
        String salt = null;
    	String hashPwd = null;
        if (mail == null ||mail.isEmpty() ||password == null ||password.isEmpty()) {
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request!");
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
            	}            }
        } catch (SQLException | NoSuchAlgorithmException e) {
            response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Wrong Query!");
        }
        String path = getServletContext().getContextPath();

        if (teacher==null && student == null){
            path = getServletContext().getContextPath() + "/Login"; // not accepted user
        }else if (teacher != null){
            request.getSession().setAttribute("teacher", teacher);
            String target = "/HomeTeacher";
            path = path + target;
        }else {
            request.getSession().setAttribute("student", student);
            String target = "/HomeStudent";
            path = path + target;
        }

        response.sendRedirect(path); // send correctly home

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
     * Implements the SHA-256 algorithm to obfuscate the password in the database 
     * 
     */
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
