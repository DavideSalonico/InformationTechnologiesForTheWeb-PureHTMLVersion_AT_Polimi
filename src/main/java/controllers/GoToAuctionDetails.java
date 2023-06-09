package controllers;

import DAO.AuctionDAO;
import DAO.OfferDAO;
import DAO.UserDAO;
import beans.Article;
import beans.Auction;
import beans.Offer;
import beans.User;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import utils.AuctionDetailsInfo;
import utils.ConnectionHandler;
import utils.Pair;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serial;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@WebServlet("/GoToAuctionDetails")
public class GoToAuctionDetails extends HttpServlet {
	@Serial
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	private OfferDAO offerDAO;
	private AuctionDAO auctionDAO;
	private UserDAO userDAO;
	
	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();

		templateEngine = utils.EngineHandler.setEngine(servletContext);
		connection = ConnectionHandler.getConnection(servletContext);

		offerDAO = new OfferDAO(connection);
		auctionDAO = new AuctionDAO(connection);
		userDAO = new UserDAO(connection);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String page;
		int auctionId;
	
		// page is a parameter that allows to distinguish between the auctionDetails.html and offer.html pages
		try{
			page = request.getParameter("page");
			if(page == null || (!page.equals("auctionDetails.html") && !page.equals("offer.html"))){
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Page parameter not valid: " + page);
				return;
			}
			auctionId = Integer.parseInt(request.getParameter("auctionId"));
		} catch(NumberFormatException e){
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "auctionId must be an integer value");
			return;
		}

		try {
			setupPage(request, response, page, auctionId);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error accessing the database");
		}
	}
	
	private void setupPage(HttpServletRequest request, HttpServletResponse response, String page, Integer auctionId) throws IOException, SQLException {
		Auction auction;
		List <Article> articles;
		Offer maxAuctionOffer = null;
		List<Offer> auctionOffers = new ArrayList<>();
		Map<Integer, String> imageMap = new HashMap<>();

		AuctionDetailsInfo auctionDetailsInfo;

		LinkedHashMap<Offer, String> frmtAuctionOffers = new LinkedHashMap<>();
		LinkedHashMap<Integer, String> users = new LinkedHashMap<>();
    	String frmtDeadline;
    	// Used to check if the auction is expired
    	LocalDateTime currLdt = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
    	boolean isExpired;
		
    	// This is the user who has won the auction, if it has been closed already
    	User awardedUser = null;
		
		try {
			auction = auctionDAO.getAuction(auctionId);
			if (auction == null) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "auctionId is not valid");
				return;
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to access the database");
			return;
		}

		try {
			auctionDetailsInfo = auctionDAO.getAuctionDetails(auctionId);
			articles = auctionDetailsInfo.getArticles();
			List<Pair<Offer, String>> tmpOffers= new ArrayList<>();
			tmpOffers = offerDAO.getOffersUsername(auctionId);
			if(!tmpOffers.isEmpty()){
				awardedUser = userDAO.getUser(tmpOffers.get(0).getFirst().getUser());
				if(awardedUser != null){
					// Removes the password from the object for security purposes
					awardedUser.setPassword("");
					awardedUser.setUser_id(0);
				}

				maxAuctionOffer = tmpOffers.get(0).getFirst();

				for(Pair<Offer, String> pair : tmpOffers){
					Offer offer = pair.getFirst();
					auctionOffers.add(offer);
					// Adds both the offer and it's formatted datetime
					frmtAuctionOffers.put(offer, offer.getTime().format(DateTimeFormatter.ofPattern("dd MMM yyyy 'alle' HH:mm")));
					if(!users.containsKey(offer.getUser())){
						// Get all "username" of the users who made an offer
						users.put(offer.getUser(), pair.getSecond());
					}
				}
			}

			if (auctionDetailsInfo == null) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "auctionId is not valid");
				return;
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to access the database");
			return;
		}

		for(Article article : articles){
			// Adds the image to the map
			imageMap.put(article.getArticle_id(), Base64.getEncoder().encodeToString(article.getImage().getBytes(1, (int) article.getImage().length())));
		}
		
		// This changes the deadline format in order to be more readable when showed in the html page
		frmtDeadline = auction.getExpiring_date().format(DateTimeFormatter.ofPattern("dd MMM yyyy 'ore' HH:mm"));
		// Checks if the auction is expired, the variable is used inside the dettagli.html and offerta.html pages
		isExpired = currLdt.isAfter(auction.getExpiring_date());
		
		// Redirect to AuctionDetails 
		String path = "/WEB-INF/" + page;
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("auctionId", auctionId);
		ctx.setVariable("auction", auction);
		ctx.setVariable("article", articles);
		ctx.setVariable("frmtDeadline", frmtDeadline);
		ctx.setVariable("isExpired", isExpired);
		ctx.setVariable("offers", frmtAuctionOffers);
		ctx.setVariable("users", users);
		ctx.setVariable("maxAuctionOffer", maxAuctionOffer);
		ctx.setVariable("awardedUser", awardedUser);
		ctx.setVariable("imageMap", imageMap);
		try{
			templateEngine.process(path, ctx, response.getWriter());
		} catch (Exception e){
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
