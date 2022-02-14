package it.polimi.tiw.purehtml.filters;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 
 * used as a filter to the servlet to correctly check if the current session is from a student
 *
 */
@WebFilter("/StudentChecker")
public class StudentChecker implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        String loginPath = httpRequest.getServletContext().getContextPath() + "/Login";

        HttpSession session = httpRequest.getSession();
        if (session.isNew() || session.getAttribute("student") == null) {
            httpResponse.sendRedirect(loginPath);
            return;
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
