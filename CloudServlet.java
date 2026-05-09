

/*import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;

public class CloudServlet extends HttpServlet {

    public Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/cloud_db",
            "root",
            "root123"
        );
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        res.setContentType("text/html");
        PrintWriter out = res.getWriter();

        String action = req.getParameter("action");
        String idParam = req.getParameter("id");
        String status = req.getParameter("status");
        String type = req.getParameter("type");

        if (status == null) status = "ACTIVE";

        try {
            Connection con = getConnection();

            // ADD LOG
            if ("add".equals(action)) {
                String[] logs = {
                    "CLOUD_ALERT: Multiple login attempts from unknown IP",
                    "CLOUD_ALERT: Suspicious login from new device",
                    "CLOUD_ALERT: Password reset request"
                };

                String log = logs[(int)(Math.random()*3)];
                String id = "A" + (int)(Math.random()*1000);

                String t = log.substring(0, log.indexOf(":")).trim();
                String msg = log.substring(log.indexOf(":")+2);

                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO alerts VALUES (?, ?, ?, ?)");
                ps.setString(1,id);
                ps.setString(2,t);
                ps.setString(3,msg);
                ps.setString(4,"ACTIVE");
                ps.executeUpdate();
            }

            // RESOLVE
            if ("resolve".equals(action)) {
                PreparedStatement ps = con.prepareStatement(
                        "UPDATE alerts SET status='RESOLVED' WHERE alertId=?");
                ps.setString(1,idParam);
                ps.executeUpdate();
            }

            // BLOCK
            if ("block".equals(action)) {
                PreparedStatement ps = con.prepareStatement(
                        "UPDATE alerts SET status='BLOCKED' WHERE alertId=?");
                ps.setString(1,idParam);
                ps.executeUpdate();
            }

            // UI
            out.println("<html><head><style>");
            out.println("body{background:#0f172a;color:white;font-family:Arial;padding:20px}");
            out.println(".top{margin-bottom:20px}");
            out.println("button{background:#38bdf8;border:none;padding:8px 14px;margin:5px;border-radius:20px;cursor:pointer}");
            out.println(".card{background:#1e293b;padding:15px;border-radius:12px;margin:12px 0;box-shadow:0 5px 10px #000}");
            out.println(".ACTIVE{color:#22c55e}");
            out.println(".RESOLVED{color:#facc15}");
            out.println(".BLOCKED{color:#ef4444}");
            out.println("</style></head><body>");

            out.println("<h2>Cloud Access Security Monitor</h2>");

            out.println("<div class='top'>");
            out.println("<a href='?status=ACTIVE'><button>Active</button></a>");
            out.println("<a href='?status=RESOLVED'><button>Resolved</button></a>");
            out.println("<a href='?status=BLOCKED'><button>Blocked</button></a>");
            out.println("<a href='?action=add'><button>Received Logs</button></a>");
            out.println("</div>");

            // TYPE FILTER
            out.println("<a href='?status="+status+"&action=typeUI'><button>Select Type</button></a>");

            if ("typeUI".equals(action)) {
                out.println("<form>");
                out.println("<input type='hidden' name='status' value='"+status+"'>");
                out.println("<select name='type'>");
                out.println("<option value='multiple'>Multiple Login</option>");
                out.println("<option value='suspicious'>Suspicious Login</option>");
                out.println("<option value='password'>Password Reset</option>");
                out.println("</select>");
                out.println("<button>Apply</button>");
                out.println("</form>");
            }

            // QUERY
            String q = "SELECT * FROM alerts WHERE status='"+status+"'";

            if (type != null) {
                if (type.equals("multiple")) q += " AND message LIKE '%Multiple%'";
                if (type.equals("suspicious")) q += " AND message LIKE '%Suspicious%'";
                if (type.equals("password")) q += " AND message LIKE '%Password%'";
            }

            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(q);

            while(rs.next()) {
                String id = rs.getString("alertId");
                String stat = rs.getString("status");

                out.println("<div class='card'>");
                out.println("<b>ID:</b> "+id+"<br>");
                out.println("<b>Message:</b> "+rs.getString("message")+"<br>");
                out.println("<b>Status:</b> <span class='"+stat+"'>"+stat+"</span><br><br>");

                if(stat.equals("ACTIVE")) {
                    out.println("<a href='?action=resolve&id="+id+"'><button>Resolve</button></a>");
                    out.println("<a href='?action=block&id="+id+"'><button>Block</button></a>");
                }

                out.println("</div>");
            }

            out.println("</body></html>");
            con.close();

        } catch(Exception e) {
            out.println("ERROR: "+e.getMessage());
        }
    }
}*/


import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;

public class CloudServlet extends HttpServlet {

    public Connection getConnection() throws Exception {
        Class.forName("org.postgresql.Driver");
        
        String dbUrl = System.getenv("DB_URL");
        if (dbUrl == null || dbUrl.isEmpty()) {
            throw new Exception("DB_URL environment variable is missing! Please set it in Render Dashboard.");
        }
        
        Connection conn;
        if (dbUrl.startsWith("postgres://") || dbUrl.startsWith("postgresql://")) {
            java.net.URI uri = new java.net.URI(dbUrl);
            String userInfo = uri.getUserInfo();
            String user = "";
            String pass = "";
            if (userInfo != null && userInfo.contains(":")) {
                user = userInfo.split(":")[0];
                pass = userInfo.split(":")[1];
            }
            String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + (uri.getPort() == -1 ? "" : ":" + uri.getPort()) + uri.getPath();
            if (uri.getQuery() != null) {
                jdbcUrl += "?" + uri.getQuery();
            }
            conn = DriverManager.getConnection(jdbcUrl, user, pass);
        } else {
            // Fallback for standard JDBC URLs
            if (!dbUrl.startsWith("jdbc:")) {
                dbUrl = "jdbc:" + dbUrl;
            }
            conn = DriverManager.getConnection(dbUrl);
        }
        
        // Ensure the alerts table exists so it works perfectly out of the box
        Statement st = conn.createStatement();
        st.execute("CREATE TABLE IF NOT EXISTS alerts (alertId VARCHAR(50) PRIMARY KEY, type VARCHAR(50), message VARCHAR(255), status VARCHAR(20))");
        st.close();
        
        return conn;
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        res.setContentType("text/html");
        PrintWriter out = res.getWriter();

        String action = req.getParameter("action");
        String idParam = req.getParameter("id");
        String status = req.getParameter("status");
        String type = req.getParameter("type");
        String sort = req.getParameter("sort"); 

        if (status == null) status = "ACTIVE";

        try {
            Connection con = getConnection();

            // ADD LOG
            if ("add".equals(action)) {
                String[] logs = {
                    "CLOUD_ALERT: Multiple login attempts from unknown IP",
                    "CLOUD_ALERT: Suspicious login from new device",
                    "CLOUD_ALERT: Password reset request"
                };

                String log = logs[(int)(Math.random()*3)];
                String id = "A" + java.util.UUID.randomUUID().toString().substring(0, 8);

                String t = log.substring(0, log.indexOf(":")).trim();
                String msg = log.substring(log.indexOf(":")+2);

                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO alerts VALUES (?, ?, ?, ?)");
                ps.setString(1,id);
                ps.setString(2,t);
                ps.setString(3,msg);
                ps.setString(4,"ACTIVE");
                ps.executeUpdate();
            }

            // RESOLVE
            if ("resolve".equals(action)) {
                PreparedStatement ps = con.prepareStatement(
                        "UPDATE alerts SET status='RESOLVED' WHERE alertId=?");
                ps.setString(1,idParam);
                ps.executeUpdate();
            }

            // BLOCK
            if ("block".equals(action)) {
                PreparedStatement ps = con.prepareStatement(
                        "UPDATE alerts SET status='BLOCKED' WHERE alertId=?");
                ps.setString(1,idParam);
                ps.executeUpdate();
            }

            // VIEW RAW DATABASE
            if ("viewdb".equals(action)) {
                out.println("<html><head><style>");
                out.println("body{background:#0f172a;color:white;font-family:Arial;padding:20px}");
                out.println("table{width:100%;border-collapse:collapse;margin-top:20px}");
                out.println("th,td{border:1px solid #334155;padding:10px;text-align:left}");
                out.println("th{background:#1e293b;color:#38bdf8}");
                out.println("</style></head><body>");
                out.println("<h2>Raw Database View (Alerts Table)</h2>");
                out.println("<a href='?'><button style='padding:8px 14px;background:#38bdf8;border:none;border-radius:20px;cursor:pointer'>Back to Dashboard</button></a>");
                out.println("<table><tr><th>Alert ID</th><th>Type</th><th>Message</th><th>Status</th></tr>");
                
                Statement stDb = con.createStatement();
                ResultSet rsDb = stDb.executeQuery("SELECT * FROM alerts");
                while(rsDb.next()) {
                    out.println("<tr>");
                    out.println("<td>" + rsDb.getString("alertId") + "</td>");
                    out.println("<td>" + rsDb.getString("type") + "</td>");
                    out.println("<td>" + rsDb.getString("message") + "</td>");
                    out.println("<td>" + rsDb.getString("status") + "</td>");
                    out.println("</tr>");
                }
                out.println("</table></body></html>");
                con.close();
                return; // Stop here, don't render the rest of the page
            }

            // UI
            out.println("<html><head><style>");
            out.println("body{background:#0f172a;color:white;font-family:Arial;padding:20px}");
            out.println(".top{margin-bottom:20px}");
            out.println("button{background:#38bdf8;border:none;padding:8px 14px;margin:5px;border-radius:20px;cursor:pointer}");
            out.println(".card{background:#1e293b;padding:15px;border-radius:12px;margin:12px 0;box-shadow:0 5px 10px #000}");
            out.println(".ACTIVE{color:#22c55e}");
            out.println(".RESOLVED{color:#facc15}");
            out.println(".BLOCKED{color:#ef4444}");
            out.println("</style></head><body>");

            out.println("<h2>Cloud Access Security Monitor</h2>");

            out.println("<div class='top'>");
            out.println("<a href='?status=ACTIVE'><button>Active</button></a>");
            out.println("<a href='?status=RESOLVED'><button>Resolved</button></a>");
            out.println("<a href='?status=BLOCKED'><button>Blocked</button></a>");
            out.println("<a href='?action=add'><button>Received Logs</button></a>");
            out.println("<a href='?action=viewdb'><button style='background:#f59e0b'>View Raw Database</button></a>");

            // SORT BUTTON ONLY FOR ACTIVE
            if (status.equals("ACTIVE")) {
                out.println("<a href='?status=ACTIVE&sort=asc'><button>Sort ID</button></a>");
            }

            out.println("</div>");

            // TYPE FILTER
            out.println("<a href='?status="+status+"&action=typeUI'><button>Select Type</button></a>");

            if ("typeUI".equals(action)) {
                out.println("<form>");
                out.println("<input type='hidden' name='status' value='"+status+"'>");
                out.println("<select name='type'>");
                out.println("<option value='multiple'>Multiple Login</option>");
                out.println("<option value='suspicious'>Suspicious Login</option>");
                out.println("<option value='password'>Password Reset</option>");
                out.println("</select>");
                out.println("<button>Apply</button>");
                out.println("</form>");
            }

            // QUERY
            String q = "SELECT * FROM alerts WHERE status='"+status+"'";

            if (type != null) {
                if (type.equals("multiple")) q += " AND message LIKE '%Multiple%'";
                if (type.equals("suspicious")) q += " AND message LIKE '%Suspicious%'";
                if (type.equals("password")) q += " AND message LIKE '%Password%'";
            }

            // SORT LOGIC
            if ("asc".equals(sort) && status.equals("ACTIVE")) {
                q += " ORDER BY alertId";
            }

            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(q);

            while(rs.next()) {
                String id = rs.getString("alertId");
                String stat = rs.getString("status");

                out.println("<div class='card'>");
                out.println("<b>ID:</b> "+id+"<br>");
                out.println("<b>Message:</b> "+rs.getString("message")+"<br>");
                out.println("<b>Status:</b> <span class='"+stat+"'>"+stat+"</span><br><br>");

                if(stat.equals("ACTIVE")) {
                    out.println("<a href='?action=resolve&id="+id+"'><button>Resolve</button></a>");
                    out.println("<a href='?action=block&id="+id+"'><button>Block</button></a>");
                }

                out.println("</div>");
            }

            out.println("</body></html>");
            con.close();

        } catch(Exception e) {
            out.println("ERROR: "+e.getMessage());
        }
    }
}