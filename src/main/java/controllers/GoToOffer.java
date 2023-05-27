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
	OfferDAO offerDAO;
	
	public GoToOffer() {
		super();
	}
	
	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		templateEngine = utils.EngineHandler.setEngine(servletContext);
		connection = ConnectionHandler.getConnection(getServletContext());;
	
		connection = ConnectionHandler.getConnection(getServletContext());
		
		offerDAO = new OfferDAO(connection);
	}
	

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	
		// get and check params
		Integer offer_id = null;
		
		try {
			offer_id = Integer.parseInt(request.getParameter("offer_id"));    //AGGIUNGERE ALL'URL
		} catch (NumberFormatException | NullPointerException e) {
			// only for debugging e.printStackTrace();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect param values");
			return;
		}
	
		
		Offer offer;
		try {
			offer = offerDAO.getOffer(offer_id);
			if (offer == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Offer not found");
				return;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			//response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover mission");
			return;
		}
	
		// Redirect to AuctionDetails 
		String path = "/WEB-INF/offer.html";    //CAPIRE SE MODIFICARE DINAMICAMENTE
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("offer", offer);
		templateEngine.process(path, ctx, response.getWriter());
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
