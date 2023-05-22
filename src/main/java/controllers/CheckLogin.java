package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import DAO.UserDAO;
import beans.User;
import utils.ConnectionHandler;

/**
 * Servlet implementation class CheckLogin
 */

@WebServlet("/CheckLogin")
public class CheckLogin extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	private UserDAO userDao;
	
    
    public CheckLogin() {
        super();
    }


	public void init() throws ServletException {   // FORSE DA METTERE IN UN TRY CATCH
		ServletContext servletContext = getServletContext();
		try {
			connection = ConnectionHandler.getConnection(servletContext);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
		
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

	//TODO: Far funzionare il filtro
	//TODO: Non mettere link a pagine ma chiamare servlet
}
