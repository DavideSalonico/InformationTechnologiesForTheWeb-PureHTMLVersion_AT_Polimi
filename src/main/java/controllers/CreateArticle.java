package controllers;

import DAO.ArticleDAO;
import beans.Article;
import beans.User;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import utils.ConnectionHandler;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Servlet implementation class CreateArticle
 */
@WebServlet("/CreateArticle")
public class CreateArticle extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	
	private ArticleDAO articleDAO;
    
	
    public CreateArticle() {
        super();
    }

    public void init() throws ServletException{
		ServletContext servletContext = getServletContext();
		templateEngine = utils.EngineHandler.setEngine(servletContext);
		connection = ConnectionHandler.getConnection(getServletContext());
		
		articleDAO = new ArticleDAO(connection);
    }
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		WebContext context = new WebContext(request, response, getServletContext(), request.getLocale());
        templateEngine.process("/WEB-INF/sell.html", context, response.getWriter());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String name = null;
		String description = null;
		String image = null;
		Integer article_creator = null;
		Integer auction_id = null;
		Integer price = null;
		
		try {
			name = (String) request.getParameter("name");
			description = (String) request.getParameter("description");
			image = (String) request.getParameter("image");
			article_creator = (((User) request.getSession().getAttribute("user")).getUser_id());
			price = Integer.parseInt(request.getParameter("price"));
			
			if(name == null || name.isEmpty() ||
				description == null || description.isEmpty() ||
				image == null || image.isEmpty() ||
				article_creator == null || price == null) {
				throw new Exception("Missing or empty credential value");
			}
		}
		catch(Exception e){
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to read parameters");
			return;
		}

		if(name.length() > 255 || description.length() > 255 || image.length() > 255) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Parameters too long");
			return;
		}
		if(price <= 0) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to read price");
			return;
		}
		if(article_creator <= 0) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to read article creator");
			return;
		}
		
		Article article = null;
		try {
			articleDAO.insertArticle(name, description,  image, price, article_creator);
		}
		catch(SQLException e){
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to insert a new articole into database"); 
			return; 
		}
		
		RequestDispatcher reqd = request.getRequestDispatcher("GoToSell");
        
        // Forward the Request Dispatcher object.
        reqd.forward(request, response);
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
