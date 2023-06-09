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
import java.util.*;

@WebServlet("/GoToSell")
public class GoToSell extends HttpServlet {
	@Serial
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
    
	private AuctionDAO auctionDAO;
	private ArticleDAO articleDAO;
	private OfferDAO offerDAO;

	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();

		templateEngine = utils.EngineHandler.setEngine(servletContext);
		connection = ConnectionHandler.getConnection(servletContext);
		
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
		setupPage(request, response);
	}
	
	private void setupPage(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
		User user = (User) request.getSession().getAttribute("user");
		LocalDateTime logLdt = (LocalDateTime) request.getSession(false).getAttribute("creationTime");

		List<Article> articles;

		// LINKED HASHMAPS ARE USED TO PRESERVE THE ORDER OF INSERTION
		LinkedHashMap<Auction,List<Article>> userOpenAuctions = new LinkedHashMap<>();
    	LinkedHashMap<Auction, List<Article>> userClosedAuctions = new LinkedHashMap<>();
    	HashMap<Integer, DiffTime> remainingTimes = new HashMap<>();
    	HashMap<Integer, Offer> maxOffers = new HashMap<>();

		List<AuctionFullInfo> userFullAuctions = new ArrayList<>();

		List <Article> articlesSelected = new ArrayList<>();
		Article chosenArticle;

		try {
			if(request.getParameterValues("alreadySelected") != null){
				String [] selectedArticles = request.getParameterValues("alreadySelected");
				for(String string : selectedArticles) {
					Article temp = articleDAO.getArticle(Integer.parseInt(string));
					articlesSelected.add(temp);
				}
			}
			if(request.getParameter("articleSelected") != null) {
				int selectedArticleId = Integer.parseInt(request.getParameter("articleSelected"));
				chosenArticle = articleDAO.getArticle(selectedArticleId);
				articlesSelected.add(chosenArticle);
			}

			userFullAuctions = auctionDAO.getAuctionsByUser(user.getUser_id());
			for(AuctionFullInfo auctionFullInfo : userFullAuctions){
				if(auctionFullInfo.getAuction().isOpen())
					userOpenAuctions.put(auctionFullInfo.getAuction(), auctionFullInfo.getArticles());
				else
					userClosedAuctions.put(auctionFullInfo.getAuction(), auctionFullInfo.getArticles());

				remainingTimes.put(auctionFullInfo.getAuction().getAuction_id(), DiffTime.getRemainingTime(logLdt, auctionFullInfo.getAuction().getExpiring_date()));
				maxOffers.put(auctionFullInfo.getAuction().getAuction_id(), auctionFullInfo.getMaxOffer());
			}

			articles = articleDAO.getAvailableUserArticles(user.getUser_id());

			if(!articlesSelected.isEmpty()) {
				for (Article x : articlesSelected) {
					articles.remove(x);
				}
			}


		}catch(SQLException e){
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover articles in database");
			return;
		}


		// Get the current LocalDateTime and creates the ldt variable
		// It is used as initial value for the datetime-local tag in sell.html
		LocalDateTime ldt = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
					
		String path = "/WEB-INF/sell.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("userOpenAuctions", userOpenAuctions);
		ctx.setVariable("userClosedAuctions", userClosedAuctions);
		ctx.setVariable("remainingTimes", remainingTimes);
		ctx.setVariable("maxOffers", maxOffers);
		ctx.setVariable("ldt", ldt);
		ctx.setVariable("articles", articles);
		ctx.setVariable("articlesSelected", articlesSelected);
		templateEngine.process(path, ctx, response.getWriter());
    }
}
