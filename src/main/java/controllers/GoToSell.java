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
import java.util.*;

@WebServlet("/GoToSell")
public class GoToSell extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
    
	private AuctionDAO auctionDAO;
	private ArticleDAO articleDAO;
	private OfferDAO offerDAO;
    
    public GoToSell() {
        super();
    }

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

	// Filter already check if the user is logged
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		setupPage(request, response);
	}
	
	private void setupPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
		User user = (User) request.getSession().getAttribute("user");
		
		// Variabili di appoggio 
		List<Auction> openAuctions= null;
		List<Auction> closedAuctions = null;
		List<Article> articles = null;

		Offer maxOffer = null;
		
		// The final result
		LinkedHashMap<Auction,Article> userOpenAuctions = new LinkedHashMap<>();
    	LinkedHashMap<Auction, Article> userClosedAuctions = new LinkedHashMap<>();
    	HashMap<Integer, DiffTime> remainingTimes = new HashMap<>();
    	HashMap<Integer, Offer> maxOffers = new HashMap<>();


		try {
			//CONTROLLA SE SONO IN ORDINE CRESCENTE DI DATA+ORA
			openAuctions = auctionDAO.getOpenAuctions(user.getUser_id());
			closedAuctions = auctionDAO.getClosedAuctions(user.getUser_id());
			
			//Manage all the user's open auction
			for (Auction auction : openAuctions ) {
				articles = articleDAO.getAuctionArticles(auction.getAuction_id());
				maxOffer = offerDAO.getWinningOffer(auction.getAuction_id());
				
				for(Article article : articles) {
					userOpenAuctions.put(auction, article);
				}
				
				LocalDateTime logLdt = (LocalDateTime) request.getSession(false).getAttribute("creationTime");
				DiffTime diff = DiffTime.getRemainingTime(logLdt, auction.getExpiring_date());
				remainingTimes.put(auction.getAuction_id(), diff);
				if(maxOffer != null)
					maxOffers.put(auction.getAuction_id(), maxOffer);
			}
			
			//Manage all the user's closed auction
			for (Auction auction : closedAuctions ) {
				articles = articleDAO.getAuctionArticles(auction.getAuction_id());
				maxOffer = offerDAO.getWinningOffer(auction.getAuction_id());
				
				for(Article article : articles) {
					userClosedAuctions.put(auction, article);
				}
				
				LocalDateTime logLdt = (LocalDateTime) request.getSession(false).getAttribute("creationTime");
				DiffTime diff = DiffTime.getRemainingTime(logLdt, auction.getExpiring_date());
				remainingTimes.put(auction.getAuction_id(), diff);
				if(maxOffer != null)
					maxOffers.put(auction.getAuction_id(), maxOffer);
			}

			articles = articleDAO.getAvailableUserArticles(user.getUser_id());

			
		}catch(SQLException e){
			e.printStackTrace();
			response.sendError(500, e.getMessage());
			return;
		}

		// Get the current LocalDateTime and creates the ldt variable
		// It is used as initial value for the datetime-local tag in sell.html
		LocalDateTime ldt = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
					
		String path = "/WEB-INF/sell.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		// PASSO VALORI ALLA PAGINA DI RITORNO 
		ctx.setVariable("userOpenAuctions", userOpenAuctions);
		ctx.setVariable("userClosedAuctions", userClosedAuctions);
		ctx.setVariable("remainingTimes", remainingTimes);
		ctx.setVariable("maxOffers", maxOffers);
		ctx.setVariable("ldt", ldt);
		ctx.setVariable("articles", articles);
		
		// QUESTO TRY and CATCH è messo solo per debuggare
		try {
			templateEngine.process(path, ctx, response.getWriter());
		}catch(Exception e) {
			response.sendError(500,e.getMessage());
		}
    }

	//DEFAULT
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String[] selectedArticles = request.getParameterValues("selectedArticles");
		String newSelected = request.getParameter("newSelected");



		Set<Integer> selectedArticlesSet = new HashSet<>();
		try{
			if(newSelected !=null && !newSelected.isEmpty())
				selectedArticlesSet.add(Integer.parseInt(newSelected));
			for(int i=0;selectedArticles!= null && i<selectedArticles.length;i++) {
				selectedArticlesSet.add(Integer.parseInt(selectedArticles[i]));
			}
		}catch(NumberFormatException ex){
			response.sendError(500, "Invalid selected article id");
			return;
		}


		ctx.setVariable("articlesSelected", articlesSelected);

	}

}
