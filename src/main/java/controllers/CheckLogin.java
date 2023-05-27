package controllers;

import DAO.UserDAO;
import beans.User;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import utils.ConnectionHandler;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;


@WebServlet("/CheckLogin")
public class CheckLogin extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	private UserDAO userDao;
    
    public CheckLogin() {
        super();
    }


	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();

		templateEngine = utils.EngineHandler.setEngine(servletContext);
		connection = ConnectionHandler.getConnection(servletContext);
		
		userDao = new UserDAO(connection);  // Initialize the connection only once, not every doPost()
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		WebContext context = new WebContext(request, response, getServletContext(), request.getLocale());
        templateEngine.process("index", context, response.getWriter());
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// obtain and escape params                                                                                
		String usrn = null;                                                                                        
		String pwd = null;     
		                                                                                                           
		try {        
			usrn = (String) request.getParameter("username");
			pwd = (String)request.getParameter("password");                                       
			                                                                                                       
			if (usrn == null || pwd == null || usrn.isEmpty() || pwd.isEmpty()) {                                  
				throw new Exception("Missing or empty credential value");                                          
			}                                                                                                      
		                                                                                                           
		} catch (Exception e) {                                                                                    
			e.printStackTrace();                                                             
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing credential value");                    
			return;                                                                                                
		}                                                                                                          
		                                                                                                           
		// query db to authenticate for user                                                                                                                                        
		User user = null;                                                                                          
		try {                                                                                                      
			user = userDao.checkCredentials(usrn, pwd);                                                            
		} catch (SQLException e) {                                                                                 
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not Possible to check credentials"); 
			return;                                                                                                
		}                                                                                                          

		// If the user exists, add info to the session and go to home page, otherwise                              
		// show login page with error message
		String path;
		if (user == null) {
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("errorMsg", "Incorrect username or password");
			path = "/index.html";
			templateEngine.process(path, ctx, response.getWriter());
		} else {
			request.getSession().setAttribute("user", user);
			request.getSession().setAttribute("creationTime", LocalDateTime.now());
			path = getServletContext().getContextPath() + "/GoToHome";
			response.sendRedirect(path);
		}                                                                                                       
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
