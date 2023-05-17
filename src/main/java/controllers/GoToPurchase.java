package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.ServletConfig;
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

import DAO.ArticleDAO;
import DAO.AuctionDAO;
import beans.Article;
import beans.Auction;
import beans.User;
import utils.DiffTime;
import utils.ConnectionHandler;

@WebServlet("/GoToPurchase")
public class GoToPurchase extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	
	AuctionDAO auc;
	ArticleDAO art;
       
    public GoToPurchase() {
        super();
    }

	public void init(ServletConfig config) throws ServletException {
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
	
		connection = ConnectionHandler.getConnection(getServletContext());
		
		// Here the AuctionDAO is initialized, try catch statement don't needed, ConnectionHandler manage already the connection 
		auc = new AuctionDAO(connection);
		art = new ArticleDAO(connection);
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
				if(request.getParameter("key") != null){
					if(filterAuctions(request, response))
						setupPage(request, response);
				}else if(request.getParameter("js") != null && request.getParameter("js").equals("visited")){
					// This method is executed only for the javascript version
					getVisitedAuctions(request, response);
					}
				// If there is no key parameter, proceeds only whit the setup
				else{
					setupPage(request, response);
				}		
	}
	
	// Method required for the javascript version
    private void getVisitedAuctions(HttpServletRequest request, HttpServletResponse response) throws IOException{}
    private void setupPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{}
   
	
	
	private boolean validateKey(String key)
    {
    	// Checks if the key contains only letters and is longer than 2 characters, but less than 21
    	if(key.matches("[a-zA-Z]+") && key.length() > 2 && key.length() < 21)
    		return true;
    	return false;
    }
	
	// This method filters all auctions by looking inside the relative articles' names and descriptions and checking if the keyword is present
    private boolean filterAuctions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
    	List<Auction> auctions = new ArrayList<>();
    	List <Article> articles = null;
    	// Used to calculate the remaining time before the expiration of 
    	LocalDateTime logLdt = (LocalDateTime) request.getSession(false).getAttribute("creationTime");
    	// Used to check if the deadline of each auction is after the current datetime
    	LocalDateTime currLdt = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
    	// The Linked Hash Maps is used because it preserves the order of the elements
    	
    	// All auctions with their articles are stored inside them
    	LinkedHashMap<Auction,List<Article>> filteredOpenAuctions = new LinkedHashMap<>();
    	// The order here is not important
    	HashMap<Integer, DiffTime> remainingTimes = new HashMap<>();
    	
    	String key = request.getParameter("key");
    	
    	if(!validateKey(key))
    	{
    		response.sendError(400, "Errore, la chiave pu� contenere solo lettere non accentate!"
    				+ " Inoltre deve avere una lunghezza compresa tra i 3 e 20 caratteri.");
    		return false;
    	}
    	// Proceeds only if the key is valid
    	else
    	{
    		try {
        		// This returns all the auctions related to the articles that contain the specified keyword
    			auctions = auc.search(key);
    		} catch (SQLException e) {
    			e.printStackTrace();
    			response.sendError(500, "Errore, accesso al database fallito!");
   				return false;
   			}
        		
       		// This means that the there is at least one auction for the given keyword
       		if(auctions != null)
       		{
   				// Iterates over the list of auctions
   				for(Auction auction : auctions)
   				{
   					// This filters the auctions by their current state and checks if their deadlines are after the datetime related
   					// to the submit of the keyword. After that it adds the auctions to the LinkedHashMap, along with their articles.
   					if(auction.isOpen() && auction.getExpiring_time().isAfter(currLdt))
   					{
   			    		try {
   							// This is used to retrieve the articles related to each auction
   			    			// There is no reason to check if the article is null, because auctions and articles are created together
   			    			// See CreateAuction, all changes to the db are committed only if there are no errors
   			    			// So, if the auction exists, the article exists too
   							articles = art.getAuctionArticles(auction.getAuction_id());
   						} catch (SQLException e) {
   							e.printStackTrace();
   							response.sendError(500, "Errore, accesso al database fallito!");
   							return false;
   						}
   			    		// Adds the auction to the LinkedHashMap along with it's article
   						filteredOpenAuctions.put(auction, articles);
   						//Get the remaining time before the expiration date of the auction
   						//Calculated from the creation time of the session
   						DiffTime diff = DiffTime.getRemainingTime(logLdt, auction.getExpiring_time());
   						remainingTimes.put(auction.getAuction_id(), diff);
   					}
   	    		}
       		}
       		// Here some attributes are set, but the request is always forwarded by setupPage.
       		
       		// Sets the LinkedHashMap, containing the auctions and the articles as an attribute of the request
       		request.setAttribute("auctions", filteredOpenAuctions);
       		// Sets the HashMap, containing the auctions and the remaing time till the expiration for each of them
       		request.setAttribute("remainingTimes", remainingTimes);
       		// Sets the key as attribute in order to use it inside the jsp page
       		request.setAttribute("key", key);
       	
       	
    	}
    	// Every time there is an error, the method returns false
    	// so it's possible to execute this line only if there are no errors
    	return true;
    }

	
	// DEFAULT
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
