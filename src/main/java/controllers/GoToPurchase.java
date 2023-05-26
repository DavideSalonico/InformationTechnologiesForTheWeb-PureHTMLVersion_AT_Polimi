package controllers;

import DAO.ArticleDAO;
import DAO.AuctionDAO;
import DAO.OfferDAO;
import beans.Article;
import beans.Auction;
import beans.Offer;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
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
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
	
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
		List<Auction> auctions = new ArrayList<>();
		List<Article> articles = null;
		// The Linked Hash Maps is used because it preserves the order of the elements
		// All auctions with their articles are stored inside them
		LinkedHashMap<Auction,List<Article>> filteredOpenAuctions = new LinkedHashMap<>();
		// The order here is not important
		HashMap<Integer, DiffTime> remainingTimes = new HashMap<>();
		//HashMap that contains all user's awarded articles along with the winning offers
		HashMap<Article, Offer> awardedArticles = new HashMap<>();

		String key = request.getParameter("key");
		if(key != null){
			filterAuctions(request, response, auctions, articles, filteredOpenAuctions, remainingTimes);
		}

		String path = "/WEB-INF/purchase.html";
		final WebContext ctx = new WebContext(request, response, getServletContext(), request.getLocale());
		// Here some attributes are set, but the request is always forwarded by setupPage.
		// Sets the LinkedHashMap, containing the auctions and the articles as an attribute of the context
		ctx.setVariable("auctions", filteredOpenAuctions);
		// Sets the HashMap, containing the auctions and the remaing time till the expiration for each of them
		request.setAttribute("remainingTimes", remainingTimes);
		ctx.setVariable("remainingTimes", remainingTimes);
		// Sets the key as attribute in order to use it inside the jsp page
		ctx.setVariable("key", key);
		try{
			templateEngine.process(path, ctx, response.getWriter());
		} catch(Exception e){
			response.sendError(500, "Errore di Thymeleaf" + e.getMessage());
			return;
		}
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
		LocalDateTime logLdt = (LocalDateTime) request.getSession(false).getAttribute("creationTime");
		// Used to check if the deadline of each auction is after the current datetime
		LocalDateTime currLdt = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
		//HashMap that contains all user's awarded articles along with the winning offers
		HashMap<Article, Offer> awardedArticles = new HashMap<>();

    	String key = request.getParameter("key");
    	
    	if(!validateKey(key)){
    		response.sendError(400, "Errore, la chiave pu� contenere solo lettere non accentate!"
    				+ " Inoltre deve avere una lunghezza compresa tra i 3 e 20 caratteri.");
    		return;
    	}
    	// Proceeds only if the key is valid
    	else{
    		try {
        		// This returns all the auctions related to the articles that contain the specified keyword
    			auctions = auctionDAO.search(key);
    		} catch (SQLException e) {
    			e.printStackTrace();
    			response.sendError(500, "Errore, accesso al database fallito!");
   				return;
   			}
        		
       		// This means that the there is at least one auction for the given keyword
       		if(auctions != null){
   				// Iterates over the list of auctions
   				for(Auction auction : auctions)
   				{
   					// This filters the auctions by their current state and checks if their deadlines are after the datetime related
   					// to the submit of the keyword. After that it adds the auctions to the LinkedHashMap, along with their articles.
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
    }

	
	// DEFAULT
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
