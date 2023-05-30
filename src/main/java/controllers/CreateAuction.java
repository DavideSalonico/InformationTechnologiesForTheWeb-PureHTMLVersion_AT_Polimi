package controllers;

import DAO.ArticleDAO;
import DAO.AuctionDAO;
import beans.User;
import utils.ConnectionHandler;

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
import java.util.ArrayList;
import java.util.List;

@WebServlet("/CreateAuction")

//@MultipartConfig is needed otherwise it will be impossible to parse the parameters as parts from a multipart form
@MultipartConfig

public class CreateAuction extends HttpServlet {
	@Serial
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private ArticleDAO articleDAO;
	private AuctionDAO auctionDAO;
       
    public CreateAuction() {
        super();
    }

	public void init() throws ServletException {
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

    
    private boolean checkDatetime(LocalDateTime deadline)
    {
    	// Checks if the datetime provided by the user is after the current one
		return deadline.isAfter(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
	}
    
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		int initial_price;
		LocalDateTime expiring_date;
		int minimum_raise;
		int creator;

		List<Integer> articlesToAdd = new ArrayList<>();

		try {
			expiring_date =  LocalDateTime.parse(request.getParameter("expiring_date")).truncatedTo(ChronoUnit.MINUTES);
			if (!checkDatetime(expiring_date)) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect datetime");
				return;
			}
			minimum_raise = Integer.parseInt(request.getParameter("minimum_raise"));
			if (minimum_raise < 0) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect minimum raise");
				return;
			}
			creator = (((User) request.getSession().getAttribute("user")).getUser_id());

			String [] stringheAppoggio = request.getParameterValues("articlesSelected" );
			if( stringheAppoggio == null ) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No articles selected to add to the auction");
				return;
			}
			for (String s : stringheAppoggio) {
				articlesToAdd.add(Integer.parseInt(s));
			}

		} catch (NumberFormatException | NullPointerException e) {
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

		String path = getServletContext().getContextPath() + "/GoToSell";
		response.sendRedirect(path);

	}
	

}
