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
import utils.AuctionFullInfo;
import utils.ConnectionHandler;
import utils.DiffTime;

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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/GoToPurchase")
public class GoToPurchase extends HttpServlet {
	@Serial
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	
	AuctionDAO auctionDAO;
	ArticleDAO articleDAO;
	OfferDAO offerDAO;

	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		templateEngine = utils.EngineHandler.setEngine(servletContext);
		connection = ConnectionHandler.getConnection(getServletContext());

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
		Map<Integer, List<Article>> awardedAuctions = new HashMap<>();
		Map<Integer, Offer> winningOffers = new HashMap<>();
		List<Auction> filteredAuctions = new ArrayList<>();
		Map<Integer, List<Article>> map = new HashMap<>();
		HashMap<Integer, DiffTime> remainingTimes = new HashMap<Integer, DiffTime>();
		LocalDateTime logLdt = null;
		LocalDateTime currLdt = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

		List<AuctionFullInfo> fullAucListWon = new ArrayList<>();
		List<AuctionFullInfo> fullAucListFilt = new ArrayList<>();

		try{
			user = (User) request.getSession().getAttribute("user");
			logLdt = ((LocalDateTime) request.getSession(false).getAttribute("creationTime")).truncatedTo(ChronoUnit.MINUTES);
		} catch (NullPointerException e){
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Errore, user not logged in correctly!");
			return;
		}

		try{
			fullAucListWon = auctionDAO.getOfferWithArticle(user.getUser_id());
			for(AuctionFullInfo auction : fullAucListWon){
				winningOffers.put(auction.getAuction().getAuction_id(), auction.getMaxOffer());
				awardedAuctions.put(auction.getAuction().getAuction_id(), auction.getArticles());
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover the winning offers");
			return;
		}

		String key = request.getParameter("key");
		if (key != null){
			if(!validateKey(key)){
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Not valid key!, key must contain only letters and be longer than 2 characters, but less than 63");
				return;
			}
			try {
				fullAucListFilt = auctionDAO.getFiltered(key, logLdt);

				for(AuctionFullInfo auction : fullAucListFilt){
					filteredAuctions.add(auction.getAuction());
					map.put(auction.getAuction().getAuction_id(), auction.getArticles());
					remainingTimes.put(auction.getAuction().getAuction_id(), DiffTime.getRemainingTime(currLdt, auction.getAuction().getExpiring_date()));
				}

			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error accessing the database!");
				return;
			}
		}

		String path = "/WEB-INF/purchase.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("key", key);
		ctx.setVariable("filteredAuctions", filteredAuctions);
		ctx.setVariable("awardedAuctions", awardedAuctions);
		ctx.setVariable("winningOffers", winningOffers);
		ctx.setVariable("map", map);
		ctx.setVariable("remainingTimes", remainingTimes);
		templateEngine.process(path, ctx, response.getWriter());
	}

	private boolean validateKey(String key){
    	// Checks if the key contains only letters and is longer than 2 characters, but less than 63
		return key.matches("[a-zA-Z]+") && key.length() > 2 && key.length() < 63;
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}


