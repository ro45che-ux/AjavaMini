import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class LoginServlet extends HttpServlet {

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String user = req.getParameter("user");
        String pass = req.getParameter("pass");

        if ("admin".equals(user) && "1234".equals(pass)) {
            res.sendRedirect("CloudServlet");
        } else {
            PrintWriter out = res.getWriter();
            out.println("<h3>Invalid Login</h3>");
            out.println("<a href='index.html'>Try Again</a>");
        }
    }
}
