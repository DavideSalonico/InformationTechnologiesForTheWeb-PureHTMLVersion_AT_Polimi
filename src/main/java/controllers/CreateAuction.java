package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import DAO.AuctionDAO;
import utils.ConnectionHandler;

@WebServlet("/CreateAuction")
public class CreateAuction extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
       
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
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
	}
	
	private void createAuction(int auction_id, int initial_price, LocalDateTime expiring_date, int minimum_raise,
			int creator) throws SQLException {
		
		AuctionDAO auc = new AuctionDAO(connection);
		auc.insertAuction(initial_price, expiring_date, minimum_raise, creator);
		
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
    
	private boolean checkName(String name)
    {
    	// Ensures the string doesn't contains only spaces and checks if it's length is between 3 and 51 characters
    	if(!name.isBlank() && name.length() > 3 && name.length() < 51)
    		return true;
    	return false;
    }
    
   
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int auction_id;
		int initial_price;
		LocalDateTime expiring_date = LocalDateTime.now().minusYears(1);
		int minimum_raise;
		int creator;
		
		try {
			auction_id = Integer.parseInt(request.getParameter("offer_id"));
			initial_price = Integer.parseInt(request.getParameter("initial_price"));
			expiring_date =  LocalDateTime.parse(request.getParameter("expiring_date")).truncatedTo(ChronoUnit.MINUTES);
			minimum_raise = Integer.parseInt(request.getParameter("minimum_raise"));
			creator = Integer.parseInt(request.getParameter("creator")); 
	
		} catch (NumberFormatException | NullPointerException e) {
			// only for debugging e.printStackTrace();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect param values");
			return;
		}
		
		try {
			createAuction(auction_id, initial_price, expiring_date, minimum_raise, creator);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
