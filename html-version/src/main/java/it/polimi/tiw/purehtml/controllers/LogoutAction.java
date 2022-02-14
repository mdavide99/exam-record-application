package it.polimi.tiw.purehtml.controllers;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 
 * Handles the logout action, clearing the saved credentials
 *
 */
@WebServlet("/LogoutAction")
public class LogoutAction extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public LogoutAction() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (session != null) { // clear not null session
            session.invalidate();
        }

        String path = getServletContext().getContextPath() + "/Login";
        response.sendRedirect(path);// redirect in login page
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
