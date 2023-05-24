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
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import beans.Article;
import beans.Auction;
import beans.Offer;
import DAO.ArticleDAO;
import DAO.AuctionDAO;
import DAO.OfferDAO;
import utils.ConnectionHandler;

// QUANDO CLICCO SU UNA DELLE ASTE TROVATE NELLA LISTA ALLORA TROVO QUESTA PAGINA

@WebServlet("/GoToAuctionDetails")
public class GoToAuctionDetails extends HttpServlet {private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	OfferDAO offerDAO;
	AuctionDAO auctionDAO;
	ArticleDAO articleDAO;
	
	public GoToAuctionDetails() {
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
		
		//Initialize DAO only once and not every doGet()
		offerDAO = new OfferDAO(connection);
		auctionDAO = new AuctionDAO(connection);
		articleDAO = new ArticleDAO(connection);
	}
	
	
	
	// TO DO : OGNI DOGET DEVE ESSERE CHIAMATA PER OGNI ASTA DI NOSTRO INTERESSE, oppure generalizza modificando questo codice 
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	
		// Page is a parameter that allows to distinguish between the auctionDetails.html and offer.html pages
		String page = request.getParameter("page");
		if(request.getParameter("auctionId") != null && (page.equals("auctionDetails.html") || page.equals("offerta.html")))
		{
			setupPage(request, response, page);
		}
		else
		{
			// The auctionId parameter is missing
			response.sendError(400, "Errore, parametri mancanti o errati nella richiesta!");
		}
	}
	
	private void setupPage(HttpServletRequest request, HttpServletResponse response, String page) throws ServletException, IOException
    {
		// get and check params
		Integer auction_id = null;
		try {
			auction_id = Integer.parseInt(request.getParameter("auction_id"));   // AGGIUNGERE PARAMETRO ALLA URL
		} catch (NumberFormatException | NullPointerException e) {
			// only for debugging e.printStackTrace();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect param values");
			return;
		}
		
		
		List<Offer> offers = null;
		List<Article> articles = null;
		// Auction bean contains already all the auction attributes
		Auction auction = null;
		
		try {
			auction = auctionDAO.getAuction(auction_id);
			if (auction == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Auction not found");
				return;
			}
			articles = articleDAO.getAuctionArticles(auction_id);
			offers = offerDAO.getOffers(auction_id);
			
		} catch (SQLException e) {
			e.printStackTrace();
			//response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover mission");
			return;
		}
		
		
		// Redirect to AuctionDetails 
		String path = "/WEB-INF/auctionDetails.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		// PASSO VALORI ALLA PAGINA DI RITORNO 
		ctx.setVariable("auction", auction);
		ctx.setVariable("articles", articles);
		ctx.setVariable("offers", offers);
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
