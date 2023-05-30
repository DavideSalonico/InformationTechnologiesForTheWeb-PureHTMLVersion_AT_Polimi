package controllers;

import DAO.ArticleDAO;
import beans.User;
import org.thymeleaf.TemplateEngine;
import utils.ConnectionHandler;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Servlet implementation class CreateArticle
 */
@WebServlet("/CreateArticle")
@MultipartConfig
public class CreateArticle extends HttpServlet {
	@Serial
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


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String name;
		String description;
		Integer article_creator;
		Integer price;
		Part image;
		try {
			name = request.getParameter("name");
			description = request.getParameter("description");
			article_creator = (((User) request.getSession().getAttribute("user")).getUser_id());
			price = Integer.parseInt(request.getParameter("price"));
			image = request.getPart("image");


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

		InputStream imageStream = checkImage(image);

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

	private InputStream checkImage(Part image) throws IOException {
		if (image != null) {
			InputStream imgStream;
			String mimeType;
			imgStream = image.getInputStream();
			String filename = image.getSubmittedFileName();
			mimeType = getServletContext().getMimeType(filename);
			// Since the user could edit the html page, he could change the input type of the image
			// And if he doesn't upload a file, mimeType is null and this would result in an unexpected server error
			if (mimeType != null)
				// Checks if the uploaded file is an image and if it has been parsed correclty
				if (imgStream != null && imgStream.available() > 0 && mimeType.startsWith("image/"))
					return imgStream;
		}
		return null;
	}
}
