package controllers;

import DAO.OfferDAO;
import beans.Offer;
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

@WebServlet("/GoToOffer")
public class GoToOffer extends HttpServlet {
	
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	private OfferDAO offerDAO;
	
	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		templateEngine = utils.EngineHandler.setEngine(servletContext);
		connection = ConnectionHandler.getConnection(servletContext);
		
		offerDAO = new OfferDAO(connection);
	}
	

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Integer offer_id = null;
		Offer offer;
		
		try {
			offer_id = Integer.parseInt(request.getParameter("offer_id"));
		} catch (NumberFormatException | NullPointerException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect offer_id value");
			return;
		}
		if(offer_id < 0) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Offer id must be positive");
			return;
		}

		try {
			offer = offerDAO.getOffer(offer_id);
			if (offer == null) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Offer not found in database");
				return;
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover mission");
			return;
		}
	
		// Redirect to AuctionDetails 
		String path = "/WEB-INF/offer.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("offer", offer);
		try{
			templateEngine.process(path, ctx, response.getWriter());
		} catch(Exception e){
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
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
