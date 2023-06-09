package controllers;

import DAO.ArticleDAO;
import beans.User;
import utils.ConnectionHandler;

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

@WebServlet("/CreateArticle")
@MultipartConfig
public class CreateArticle extends HttpServlet {
	@Serial
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private ArticleDAO articleDAO;

    public void init() throws ServletException{
		connection = ConnectionHandler.getConnection(getServletContext());

		articleDAO = new ArticleDAO(connection);
    }


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String name;
		String description;
		int article_creator;
		int price;
		Part image;
		try {
			name = request.getParameter("name");
			description = request.getParameter("description");
			article_creator = (((User) request.getSession().getAttribute("user")).getUser_id());
			price = Integer.parseInt(request.getParameter("price"));
			image = request.getPart("image");


			if(name == null || name.isEmpty()){
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or empty name value");
				return;
			}
			if(description == null || description.isEmpty()){
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or empty description value");
				return;
			}
			if(article_creator == 0){
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing article creator value");
				return;
			}
			if(price == 0){
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing price value");
				return;
			}
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect values");
			return;
		} catch (ServletException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unable to get the image");
			return;
		}

		if(name.length() < 4 || name.length() > 255 ) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Name must be between 4 and 255 characters");
			return;
		}
		if(description.length() < 10 || description.length() > 255){
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Description must be between 4 and 255 characters");
			return;
		}
		if(price <= 0 || price > 1000000000){
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Price must be between 1 and 1000000000");
			return;
		}
		if(article_creator <= 0) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unable to read article creator, must be a positive integer");
			return;
		}

		InputStream imageStream = checkImage(image, response);
		if(imageStream == null)
			return;

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

	private InputStream checkImage(Part image, HttpServletResponse response) throws IOException {
		final long maxSize = 2 * 1024 * 1024; // 2MB
		if (image != null) {
			InputStream imgStream;
			String mimeType;
			imgStream = image.getInputStream();
			String filename = image.getSubmittedFileName();
			mimeType = getServletContext().getMimeType(filename);
			// Since the user could edit the html page, he could change the input type of the image
			// And if he doesn't upload a file, mimeType is null and this would result in an unexpected server error

			// Checks if the uploaded file is an image and if it has been parsed correclty
			if (mimeType != null && imgStream != null && imgStream.available() > 0 && mimeType.startsWith("image/") && image.getSize() <= maxSize) {
				return imgStream;
			}
			else {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Image size exceeds the maximum limit (2MB)");
				if (imgStream != null) {
					imgStream.close();
				}
				return null;
			}
		}
		else{
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Wrong image format");
			return null;
		}
	}
}
