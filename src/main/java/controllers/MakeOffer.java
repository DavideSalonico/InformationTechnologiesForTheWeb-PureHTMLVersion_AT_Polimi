package controllers;

import DAO.AuctionDAO;
import DAO.OfferDAO;
import beans.Auction;
import beans.Offer;
import beans.User;
import utils.ConnectionHandler;

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

@WebServlet("/MakeOffer")
public class MakeOffer extends HttpServlet {
	@Serial
	private static final long serialVersionUID = 1L;
	private Connection connection;
	private OfferDAO offerDAO;
	private AuctionDAO auctionDAO;

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());

		offerDAO = new OfferDAO(connection);
		auctionDAO = new AuctionDAO(connection);
	}

	public void destroy() {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// Page is a parameter that allows to distinguish between the dettagli.html and offerta.html pages
		if(request.getParameter("auctionId") != null ){
			makeOffer(request, response);
		}
	}

	private boolean checkValue(int offerValue, Auction auction, Offer maxAuctionOffer)
	{
		// Min sets the minum value the user can submit
		int min;
		// 2 billions is the maximum value allowed
		int max = 1000000000;

		// If there is at least an offer for the specified auction
		// the minimum value is equal to the minimum upside offer plus
		// the value of the maximum offer
		if(maxAuctionOffer != null)
			min = auction.getMinimum_raise() + maxAuctionOffer.getPrice();
			// If there are no offers for the specified auction
			// the minimum value is equal to the initial price of the auction
		else
			min = auction.getInitial_price();

		// The offer's value is correct if it stays in between
		return min <= offerValue && offerValue <= max;
	}
	private void makeOffer(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Auction auction;
		Offer maxAuctionOffer;
		LocalDateTime currLdt = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
		User user = (User) request.getSession(false).getAttribute("user");
		// Used once the offer has been created to redirect to GetAuctionsDetails
		int aucId, offerValue;
		String strAucId;

		try {
			// These retrieve the auctionId and the value of the offer
			strAucId = request.getParameter("auctionId");
			aucId = Integer.parseInt(strAucId);
			offerValue = Integer.parseInt(request.getParameter("offer"));
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "auctionIn and offer must be integers!");
			return;
		}

		try {
			// This return the Auction object related to the specified auction if it exists or null
			auction = auctionDAO.getAuction(aucId);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Auction not found or access to database failed!");
			return;
		}

		if (auction != null) {
			// Checks if the logged user has created the auction
			if (auction.getCreator() == (user.getUser_id())) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error. You can't make an offer for your own auction!");
				return;
			}

			try {
				// This returns the Offer object related to the maximum offer for specified auction if it exists or null
				maxAuctionOffer = offerDAO.getWinningOffer(aucId);
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error. Access to database failed! Cannot retrieve the maximum offer!");
				return;
			}

			// This checks, if there is a maximum offer, if it belongs to the logged user
			// If so, the user is not allowed to make another offer
			if (maxAuctionOffer != null && user.getUser_id() == (maxAuctionOffer.getUser())) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error. You have to wait for another user to make an offer!");
				return;
			}
			if (checkValue(offerValue, auction, maxAuctionOffer)) {
				try {
					// This returns true if the Offer has been added to the database
					int outcome = offerDAO.insertOffer(offerValue, currLdt, user.getUser_id(), aucId);
					if (outcome == 0) {
						response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore, accesso al database fallito! Could not insert the offer!");
						return;
					}
				} catch (SQLException e) {
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while inserting the offer!");
					return;
				}
			} else {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error, the offer's value is not valid! " +
						"Your offer must be greater than the minimum raise plus the maximum offer's value!" +
						" If there are no offers, your offer must be greater than the initial price!" +
						" Max value allowed: 1 billion!");
			}

			String path = getServletContext().getContextPath() +"/GoToAuctionDetails?auctionId=" + strAucId + "&page=offer.html";
			response.sendRedirect(path);

		} else {
			// The given ID doesn't belong to any of the auctions
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Specified auction doesn't exist!");
		}
	}

}
