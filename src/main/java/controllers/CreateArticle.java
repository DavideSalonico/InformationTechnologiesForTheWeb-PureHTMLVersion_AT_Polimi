package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import DAO.ArticleDAO;
import beans.Article;
import utils.ConnectionHandler;

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

    public void init() {
    	ServletContext servletContext = getServletContext();
		try {
			connection = ConnectionHandler.getConnection(servletContext);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
		
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
			article_creator = Integer.parseInt(request.getParameter("article_creator"));
			auction_id = Integer.parseInt(request.getParameter("auction_id"));
			price = Integer.parseInt(request.getParameter("price"));
			
			if(name == null || name.isEmpty() ||
				description == null || description.isEmpty() ||
				image == null || image.isEmpty() ||
				article_creator == null || auction_id == null || price == null) {
				throw new Exception("Missing or empty credential value");
			}
		}
		catch(Exception e){
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to read parameters");
			return;
		}

		if(name.length() > 255 || description.length() > 255 || image.length() > 255) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to read parameters");
			return;
		}
		if(price <= 0) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to read parameters");
			return;
		}
		if(article_creator <= 0 || auction_id <= 0) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to read parameters");
			return;
		}
		
		Article article = null;
		try {
			articleDAO.insertArticle(name, description, image, price, article_creator);
		}
		catch(SQLException e){
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to insert a new articole into database"); 
			return; 
		}
		
		final WebContext ctx = new WebContext(request, response, getServletContext(), request.getLocale());
		ctx.setVariable("createdArticle", article);
		templateEngine.process("/sell.html", ctx, response.getWriter());
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
