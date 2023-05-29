package controllers;

import DAO.ArticleDAO;
import DAO.AuctionDAO;
import DAO.OfferDAO;
import DAO.UserDAO;
import beans.Article;
import beans.Auction;
import beans.Offer;
import beans.User;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;

// QUANDO CLICCO SU UNA DELLE ASTE TROVATE NELLA LISTA ALLORA TROVO QUESTA PAGINA

@WebServlet("/GoToAuctionDetails")
public class GoToAuctionDetails extends HttpServlet {private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	private OfferDAO offerDAO;
	private AuctionDAO auctionDAO;
	private ArticleDAO articleDAO;
	private UserDAO userDAO;
	
	public GoToAuctionDetails() {
		super();
	}
	
	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();

		templateEngine = utils.EngineHandler.setEngine(servletContext);
		connection = ConnectionHandler.getConnection(servletContext);

		offerDAO = new OfferDAO(connection);
		auctionDAO = new AuctionDAO(connection);
		articleDAO = new ArticleDAO(connection);
		userDAO = new UserDAO(connection);
	}
	
	
	
	// TODO : OGNI DOGET DEVE ESSERE CHIAMATA PER OGNI ASTA DI NOSTRO INTERESSE, oppure generalizza modificando questo codice
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String page = null;
		Integer auctionId = null;
	
		// Page is a parameter that allows to distinguish between the auctionDetails.html and offer.html pages
		try{
			page = request.getParameter("page");
			auctionId = Integer.parseInt(request.getParameter("auctionId"));
		} catch(NumberFormatException e){
			response.sendError(400, "Try-catch fallito" + e.getMessage());
		}
		if((page.equals("auctionDetails.html") || page.equals("offer.html"))){
			try {
				setupPage(request, response, page, auctionId);
			} catch (SQLException e) {
				response.sendError(400, "Page parameter not valid: " + page);
			}
		}
	}
	
	private void setupPage(HttpServletRequest request, HttpServletResponse response, String page, Integer auctionId) throws ServletException, IOException, SQLException {
		// get and check params
		Auction auction;
		List <Article> articles;
		Offer maxAuctionOffer;
		List<Offer> auctionOffers;
		
		//This HashMap contains all the offers with their creationTimes, formatted properly
		LinkedHashMap<Offer, String> frmtAuctionOffers = null;
		LinkedHashMap<Integer, String> users = new LinkedHashMap<>();
    	String frmtDeadline = null;
    	// Used to check if the auction is expired
    	LocalDateTime currLdt = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
    	boolean isExpired = false;
		
    	// This is the user who has won the auction, if it has been closed already
    	User awardedUser = null;
    	
    	
		try {
			auctionId = Integer.parseInt(request.getParameter("auctionId"));   // AGGIUNGERE PARAMETRO ALLA URL
		} catch (NumberFormatException | NullPointerException e) {
			response.sendError(400, "Errore, l' id deve essere un numero intero!" );
    		return;
		}
		
		try {
			auction = auctionDAO.getAuction(auctionId);
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
			articles = articleDAO.getAuctionArticles(auctionId);
			auctionOffers = offerDAO.getOffers(auctionId);
			maxAuctionOffer = offerDAO.getWinningOffer(auctionId);
			
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
			response.sendError(500, "Errore, quando cerco le offerte!");
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
				if(!users.containsKey(offer.getUser())){
					// Get all "username" of the users who made an offer
					users.put(offer.getUser(), userDAO.getUser(offer.getUser()).getUsername());
				}
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
		ctx.setVariable("auctionId", auctionId);
		ctx.setVariable("auction", auction);
		ctx.setVariable("article", articles);  // OCCHIO CHE qua è settato senza S, è una lista però
		ctx.setVariable("frmtDeadline", frmtDeadline);
		ctx.setVariable("isExpired", isExpired);
		ctx.setVariable("offers", frmtAuctionOffers);
		ctx.setVariable("users", users);
		ctx.setVariable("maxAuctionOffer", maxAuctionOffer);
		ctx.setVariable("awardedUser", awardedUser);
		// This actually processes the template page
		// QUESTO TRY and CATCH è messo solo per debuggare
		try {
			templateEngine.process(path, ctx, response.getWriter());
		}catch(Exception e) {
			response.sendError(500,e.getMessage());
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
