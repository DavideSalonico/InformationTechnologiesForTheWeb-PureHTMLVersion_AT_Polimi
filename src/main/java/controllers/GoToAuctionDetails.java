package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
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
import beans.User;
import DAO.ArticleDAO;
import DAO.AuctionDAO;
import DAO.OfferDAO;
import DAO.UserDAO;
import utils.ConnectionHandler;

// QUANDO CLICCO SU UNA DELLE ASTE TROVATE NELLA LISTA ALLORA TROVO QUESTA PAGINA

@WebServlet("/GoToAuctionDetails")
public class GoToAuctionDetails extends HttpServlet {private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	OfferDAO offerDAO;
	AuctionDAO auctionDAO;
	ArticleDAO articleDAO;
	UserDAO userDAO;
	
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
		userDAO = new UserDAO(connection);
	}
	
	
	
	// TO DO : OGNI DOGET DEVE ESSERE CHIAMATA PER OGNI ASTA DI NOSTRO INTERESSE, oppure generalizza modificando questo codice 
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	
		// Page is a parameter that allows to distinguish between the auctionDetails.html and offer.html pages
		String page = request.getParameter("page");
		if(request.getParameter("auction_id") != null && (page.equals("auctionDetails.html") || page.equals("offer.html")))
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
		Auction auction = null;
		List <Article> articles = null;
		Offer maxAuctionOffer = null;
		List<Offer> auctionOffers = null;
		
		//This HashMap contains all the offers with their creationTimes, formatted properly
		LinkedHashMap<Offer, String> frmtAuctionOffers = null;
    	String frmtDeadline = null;
    	// Used to check if the auction is expired
    	LocalDateTime currLdt = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
    	boolean isExpired = false;
		
    	// This is the user who has won the auction, if it has been closed already
    	User awardedUser = null;
    	
    	
		try {
			auction_id = Integer.parseInt(request.getParameter("auction_id"));   // AGGIUNGERE PARAMETRO ALLA URL
		} catch (NumberFormatException | NullPointerException e) {
			// only for debugging e.printStackTrace();
			response.sendError(400, "Errore, l' id deve essere un numero intero!" );
    		return;
		}
		
		try {
			auction = auctionDAO.getAuction(auction_id);
			if (auction == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Auction not found");
				return;
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(500, "Errore, accesso al database fallito!"+ e.getMessage());
			return;
		}
		
		try {
			articles = articleDAO.getAuctionArticles(auction_id);
			auctionOffers = offerDAO.getOffers(auction_id);
			maxAuctionOffer = offerDAO.getWinningOffer(auction_id);
			
			if(maxAuctionOffer != null)
			{
				// Since there is a maximum offer that belongs to the user, the user surely exists
				awardedUser = userDAO.getUser(maxAuctionOffer.getUser());
				
				if(awardedUser != null)
				{
					// Removes the password from the object for security purposes
					awardedUser.setPassword("");							
				}
			}
			
		}catch(SQLException e) {
			e.printStackTrace();
			response.sendError(500, "Errore, accesso al database fallito!");
			return;
		}
		
		// Proceeds only if there is at least 1 offer for the auction
		if(auctionOffers != null)
		{	// Initializes the HashMap
			frmtAuctionOffers = new LinkedHashMap<>();
			// Iterates over the list of offers and formats the datetime properly
    		for(Offer offer : auctionOffers)
    		{
    			String frmtOfferTime = offer.getTime().format(DateTimeFormatter.ofPattern("dd MMM yyyy 'alle' HH:mm"));
    			// Adds both the offer and it's formatted datetime
    			frmtAuctionOffers.put(offer, frmtOfferTime);
    		}
		}
		
		// This changes the deadline format in order to be more readable when showed in the html page
		frmtDeadline = auction.getExpiring_date().format(DateTimeFormatter.ofPattern("dd MMM yyyy 'ore' HH:mm"));
		// Checks if the auction is expired, the variable is used inside the dettagli.html and offerta.html pages
		isExpired = currLdt.isAfter(auction.getExpiring_date());
		
		// Redirect to AuctionDetails 
		String path = "/WEB-INF/" + page;
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		// PASSO VALORI ALLA PAGINA DI RITORNO 
		// Creates and sets 7 variables to use inside the 2 template pages
		ctx.setVariable("auction", auction);
		ctx.setVariable("article", articles);  // OCCHIO CHE qua è settato senza S, è una lista però
		ctx.setVariable("frmtDeadline", frmtDeadline);
		ctx.setVariable("isExpired", isExpired);
		ctx.setVariable("offers", frmtAuctionOffers);
		ctx.setVariable("maxAuctionOffer", maxAuctionOffer);
		ctx.setVariable("awardedUser", awardedUser);
		// This actually processes the template page
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
