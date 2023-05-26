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
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@WebServlet("/CreateAuction")

//@MultipartConfig is needed otherwise it will be impossible to parse the parameters as parts from a multipart form
@MultipartConfig

public class CreateAuction extends HttpServlet {
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

		
		try {
			//auction_id = Integer.parseInt(request.getParameter("offer_id"));
			expiring_date =  LocalDateTime.parse(request.getParameter("expiring_date")).truncatedTo(ChronoUnit.MINUTES);
			minimum_raise = Integer.parseInt(request.getParameter("minimum_raise"));
			creator = (((User) request.getSession().getAttribute("user")).getUser_id());
	
		} catch (NumberFormatException | NullPointerException e) {
			// only for debugging e.printStackTrace();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect param values");
			return;
		}
		
		try {
			auctionDAO.insertAuction(expiring_date, minimum_raise, creator);
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
