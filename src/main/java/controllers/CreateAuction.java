package controllers;

import DAO.ArticleDAO;
import DAO.AuctionDAO;
import beans.User;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import utils.ConnectionHandler;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
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
import java.util.List;

@WebServlet("/CreateAuction")

//@MultipartConfig is needed otherwise it will be impossible to parse the parameters as parts from a multipart form
@MultipartConfig

public class CreateAuction extends HttpServlet {
	@Serial
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	private ArticleDAO articleDAO;
	private AuctionDAO auctionDAO;
       
    public CreateAuction() {
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
		
		articleDAO = new ArticleDAO(connection);
		auctionDAO = new AuctionDAO(connection);
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private boolean checkNumbers(int initialPrice, int minUpsideOffer)
    {
      	if(initialPrice > 0 && initialPrice < 700000001 && minUpsideOffer > 49 && minUpsideOffer < 100001)
      		return true;
      	return false;
    }
    
    private boolean checkDatetime(LocalDateTime deadline)
    {
    	// Checks if the datetime provided by the user is after the current one
    	if(deadline.isAfter(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)))
    		return true;
    	return false;
    }
    
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int initial_price;
		LocalDateTime expiring_date = LocalDateTime.now().minusYears(1);
		int minimum_raise;
		int creator;
		// LISTA DEGLI ID degli articoli da aggiungere all'asta
		List<Integer> articlesToAdd = null;

		
		try {
			expiring_date =  LocalDateTime.parse(request.getParameter("expiring_date")).truncatedTo(ChronoUnit.MINUTES);
			minimum_raise = Integer.parseInt(request.getParameter("minimum_raise"));
			creator = (((User) request.getSession().getAttribute("user")).getUser_id());

			String [] stringheAppoggio = request.getParameterValues("selectedArticles");
			if( stringheAppoggio == null ) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No articles selected to add to the auction");
				return;
			}
			for ( int i = 0; i<stringheAppoggio.length; i++) {
				articlesToAdd.add(Integer.parseInt(stringheAppoggio[i]));
			}

		} catch (NumberFormatException | NullPointerException e) {
			// only for debugging e.printStackTrace();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect param values");
			return;
		}

		// INSERT AUCTION, LINK ALL THE ARTICLES TO THE AUCTION, SET INITIAL PRICE OF AUCTION
		try {
			int auction_id = auctionDAO.insertAuction(expiring_date, minimum_raise, creator);
			for (Integer id : articlesToAdd){
				articleDAO.addToAuction(id, auction_id);
			}
			initial_price = articleDAO.getAuctionInitialPrice(auction_id);
			auctionDAO.setInitialPrice(auction_id, initial_price);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		RequestDispatcher reqd = request.getRequestDispatcher("GoToSell");
        
        // Forward the Request Dispatcher object.
        reqd.forward(request, response);



	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// No needed
	}
	

}
