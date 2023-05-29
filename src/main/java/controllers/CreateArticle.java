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
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
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


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String name = null;
		String description = null;
		//Part image = null;
		Integer article_creator = null;
		Integer auction_id = null;
		Integer price = null;
		InputStream imageStream = null;
		try {
			name = (String) request.getParameter("name");
			description = (String) request.getParameter("description");
			//image = request.getPart("image");
			article_creator = (((User) request.getSession().getAttribute("user")).getUser_id());
			price = Integer.parseInt(request.getParameter("price"));
			imageStream = request.getPart("image").getInputStream();


			if(name == null || name.isEmpty() ||
				description == null || description.isEmpty() ||
				article_creator == null || price == null) {
				throw new Exception("Missing or empty credential value");
			}
		}
		catch(Exception e){
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to read parameters");
			return;
		}

		if(name.length() > 255 || description.length() > 255 ) {
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
		

		try {
			articleDAO.insertArticle(name, description, price, article_creator, imageStream);
		}
		catch(SQLException e){
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to insert a new article into database");
			return; 
		}

		String path = getServletContext().getContextPath() + "/GoToSell";
		response.sendRedirect(path);
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
