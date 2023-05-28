package controllers;

import DAO.ArticleDAO;
import DAO.AuctionDAO;
import DAO.OfferDAO;
import beans.Article;
import beans.Auction;
import beans.Offer;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import utils.ConnectionHandler;
import utils.DiffTime;

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
import java.time.chrono.ChronoLocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

@WebServlet("/GoToPurchase")
public class GoToPurchase extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	
	AuctionDAO auctionDAO;
	ArticleDAO articleDAO;
	OfferDAO offerDAO;

    public GoToPurchase() {
        super();
    }

	public void init() throws ServletException {

		ServletContext servletContext = getServletContext();
		templateEngine = utils.EngineHandler.setEngine(servletContext);
		connection = ConnectionHandler.getConnection(getServletContext());

		// Here the AuctionDAO is initialized, try catch statement don't needed, ConnectionHandler manage already the connection
		auctionDAO = new AuctionDAO(connection);
		articleDAO = new ArticleDAO(connection);
		offerDAO = new OfferDAO(connection);
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int userId;
		List<Offer> awardedOffers = null;
		List<Auction> awardedAuctions = new ArrayList<>();
		try {
			userId = (int) request.getSession().getAttribute("userId");
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing user id");
			return;
		}

		// If the user is not logged in (not present in session) redirect to the login
		if (userId == 0) {
			response.sendRedirect(getServletContext().getContextPath() + "/index.html");
			return;
		}

		try{
			List<Integer> auctionIds = offerDAO.getWinningOfferByUser(userId);
			for(Integer auc : auctionIds){
				awardedAuctions.add(auctionDAO.getAuction(auc));
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover the winning offers");
			return;
		}

		// If the user is logged in (present in session) redirect to the home page
		String path = "/WEB-INF/Purchase.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		templateEngine.process(path, ctx, response.getWriter());
	}

	private boolean validateKey(String key){
    	// Checks if the key contains only letters and is longer than 2 characters, but less than 21
    	if(key.matches("[a-zA-Z]+") && key.length() > 2 && key.length() < 21)
    		return true;
    	return false;
    }

	// This method filters all auctions by looking inside the relative articles' names and descriptions and checking if the keyword is present
	// TODO: This method can be optimized with better queries to database
    private void filterAuctions(HttpServletRequest request,
								HttpServletResponse response,
								List<Auction> auctions,
								List<Article> articles,
								LinkedHashMap<Auction, List<Article>> filteredOpenAuctions,
								HashMap<Integer, DiffTime> remainingTimes) throws ServletException, IOException{
		Offer maxOffer;
		// Used to calculate the remaining time before the expiration of
		// Used to check if the deadline of each auction is after the current datetime
		//HashMap that contains all user's awarded articles along with the winning offers
		HashMap<Article, Offer> awardedArticles = new HashMap<>();

    	// Proceeds only if the key is valid
        		
       		// This means that the there is at least one auction for the given keyword
       		if(auctions != null){
   				// Iterates over the list of auctions
   				for(Auction auction : auctions)
   				{
   					// This filters the auctions by their current state and checks if their deadlines are after the datetime related
   					// to the submit of the keyword. After that it adds the auctions to the LinkedHashMap, along with their articles.
					ChronoLocalDateTime<?> currLdt = null;
					if(auction.isOpen() && auction.getExpiring_date().isAfter(currLdt))
   					{
   			    		try {
   							// This is used to retrieve the articles related to each auction
   			    			// There is no reason to check if the article is null, because auctions and articles are created together
   			    			// See CreateAuction, all changes to the db are committed only if there are no errors
   			    			// So, if the auction exists, the article exists too
   							articles = articleDAO.getAuctionArticles(auction.getAuction_id());
							maxOffer = offerDAO.getWinningOffer(auction.getAuction_id());
   						} catch (SQLException e) {
   							e.printStackTrace();
   							response.sendError(500, "Errore, accesso al database fallito!");
   							return;
   						}
   			    		// Adds the auction to the LinkedHashMap along with it's article
   						filteredOpenAuctions.put(auction, articles);
   						//Get the remaining time before the expiration date of the auction
   						//Calculated from the creation time of the session
						LocalDateTime logLdt = null;
						DiffTime diff = DiffTime.getRemainingTime(logLdt, auction.getExpiring_date());
   						remainingTimes.put(auction.getAuction_id(), diff);
						for(Article article : articles){
						   if(maxOffer != null)
								awardedArticles.put(article, maxOffer);
						}

   					}
   	    		}
       		}
    	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String key = request.getParameter("key");
		LocalDateTime logLdt = ((LocalDateTime) request.getSession(false).getAttribute("creationTime")).truncatedTo(ChronoUnit.MINUTES);
		LocalDateTime currLdt = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
		int user = (int) request.getSession().getAttribute("user");

		List<Auction> filteredAuctions = new ArrayList<>();

		if(validateKey(key) == false){
			response.sendError(400, "Errore, chiave di ricerca non valida!");
			return;
		}

		if (key != null) {

		} else {
			try {
				filteredAuctions = auctionDAO.search(key, logLdt, user);
			} catch (SQLException e) {
				e.printStackTrace();
				response.sendError(500, "Errore, accesso al database fallito!");
				return;
			}

			String path = "/WEB-INF/purchase.html";
			final WebContext ctx = new WebContext(request, response, getServletContext(), request.getLocale());
			ctx.setVariable("key", key);
			ctx.setVariable("filteredAuctions", filteredAuctions);
			templateEngine.process(path, ctx, response.getWriter());
		}

	}
}


