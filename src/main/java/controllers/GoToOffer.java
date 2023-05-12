package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import DAO.OfferDAO;
import beans.Offer;
import utils.ConnectionHandler;

@WebServlet("/GoToOffer")
public class GoToOffer extends HttpServlet {
	
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	
	public GoToOffer() {
		super();
	}
	
	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
	
		connection = ConnectionHandler.getConnection(getServletContext());
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	
		// If the user is not logged in (not present in session) redirect to the login
		String loginpath = getServletContext().getContextPath() + "/index.html";
		HttpSession session = request.getSession();
		if (session.isNew() || session.getAttribute("user") == null) {
			response.sendRedirect(loginpath);
			return;
		}
	
		// get and check params
		Integer offer_id = null;
		try {
			offer_id = Integer.parseInt(request.getParameter("offer_id"));
		} catch (NumberFormatException | NullPointerException e) {
			// only for debugging e.printStackTrace();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect param values");
			return;
		}
	
		OfferDAO offerDAO = new OfferDAO(connection);
		Offer offer = new Offer();
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
		String path = "/WEB-INF/Offer.html";
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
