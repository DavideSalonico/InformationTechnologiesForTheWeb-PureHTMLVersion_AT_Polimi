package controllers;

import DAO.ArticleDAO;
import DAO.AuctionDAO;
import DAO.OfferDAO;
import beans.Article;
import beans.Auction;
import beans.Offer;
import beans.User;
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
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		User user;
		Map<Integer, List<Article>> awardedAuctions = new HashMap();
		Map<Integer, Offer> winningOffers;
		List<Auction> filteredAuctions = null;
		Map<Integer, List<Article>> map = new HashMap<>();
		HashMap<Integer, DiffTime> remainingTimes = new HashMap<Integer, DiffTime>();

		user = (User) request.getSession().getAttribute("user");
		String key = request.getParameter("key");
		LocalDateTime logLdt = ((LocalDateTime) request.getSession(false).getAttribute("creationTime")).truncatedTo(ChronoUnit.MINUTES);
		LocalDateTime currLdt = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

		try{
			winningOffers = offerDAO.getWinningOfferByUser(user.getUser_id());
			for(Integer auction : winningOffers.keySet()){
				awardedAuctions.put(auction, articleDAO.getAuctionArticles(auction));
				/// IN TEORIA QUA NON SERVE map.put(auction, articleDAO.getAuctionArticles(auction));
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover the winning offers");
			return;
		}

		if (key != null){
			if(validateKey(key) == false){
				response.sendError(400, "Errore, chiave di ricerca non valida!");
				return;
			}
			try {
				filteredAuctions = auctionDAO.search(key, logLdt);
			} catch (SQLException e) {
				e.printStackTrace();
				response.sendError(500, "Errore, accesso al database fallito!" + e.getMessage());
				return;
			}
		}

		if(filteredAuctions != null){
			for(Auction auction : filteredAuctions){
				try {
					map.put(auction.getAuction_id(), articleDAO.getAuctionArticles(auction.getAuction_id()));
					remainingTimes.put(auction.getAuction_id(), DiffTime.getRemainingTime(currLdt, auction.getExpiring_date()));
				} catch (SQLException e) {
					e.printStackTrace();
					response.sendError(500, "Errore, accesso al database fallito!" + e.getMessage());
					return;
				}
			}
		}

		// If the user is logged in (present in session) redirect to the home page
		String path = "/WEB-INF/purchase.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("key", key);
		ctx.setVariable("filteredAuctions", filteredAuctions);
		ctx.setVariable("awardedAuctions", awardedAuctions);
		ctx.setVariable("winningOffers", winningOffers);
		ctx.setVariable("map", map);
		ctx.setVariable("remainingTimes", remainingTimes);
		try{
			templateEngine.process(path, ctx, response.getWriter());
		} catch (Exception e){
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover the page" + e.getMessage());
		}
		//aggiunto commento a caso per fare un commit


	}

	private boolean validateKey(String key){
    	// Checks if the key contains only letters and is longer than 2 characters, but less than 21
    	if(key.matches("[a-zA-Z]+") && key.length() > 2 && key.length() < 21)
    		return true;
    	return false;
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}


