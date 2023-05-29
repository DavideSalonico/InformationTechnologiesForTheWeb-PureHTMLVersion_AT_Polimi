package controllers;

import DAO.AuctionDAO;
import DAO.OfferDAO;
import beans.Auction;
import beans.Offer;
import beans.User;
import org.thymeleaf.TemplateEngine;
import utils.ConnectionHandler;

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

/**
 * Servlet implementation class MakeOffer
 */
@WebServlet("/MakeOffer")
public class MakeOffer extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
	private TemplateEngine templateEngine;
	private OfferDAO offerDAO;
	private AuctionDAO auctionDAO;

	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		templateEngine = utils.EngineHandler.setEngine(servletContext);
		connection = ConnectionHandler.getConnection(getServletContext());
		offerDAO = new OfferDAO(connection);
		auctionDAO = new AuctionDAO(connection);
	}

	public void destroy() {
		try {
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Page is a parameter that allows to distinguish between the dettagli.html and offerta.html pages
		if(request.getParameter("auctionId") != null )
		{
			makeOffer(request, response);
		}
		else
		{
			// The auctionId parameter is missing
			response.sendError(400, "Errore, parametri mancanti nella richiesta (auctionID non trovato)!");
		}
	}

	private boolean checkValue(int offerValue, Auction auction, Offer maxAuctionOffer)
	{
		// Min sets the minum value the user can submit
		int min = 0;
		// 2 billions is the maximum value allowed
		int max = 2000000000;

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
		if(min <= offerValue && offerValue <= max)
			return true;
		return false;
	}
	private void makeOffer(HttpServletRequest request, HttpServletResponse response) throws IOException {

		Auction auction = null;
		Offer maxAuctionOffer = null;
		LocalDateTime currLdt = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
		User user = (User) request.getSession(false).getAttribute("user");
		// Used once the offer has been created to redirect to GetAuctionsDetails
		// In order to work, the controller needs 2 parameters, the auctionId and the page to process
		String strAucId = request.getParameter("auctionId");
		int aucId, offerValue;
		// Used to check if the offer has been added correctly
		boolean result = false;

		try {
			// These retrieve the auctionId and the value of the offer
			aucId = Integer.parseInt(strAucId);
			//Il parametro offer contiene il valore dell'offerta
			offerValue = Integer.parseInt(request.getParameter("offer"));
		} catch (Exception e) {
			// If the values can't be parsed they are not formatted correctly
			e.printStackTrace();
			response.sendError(400, "Errore, l'id dell' asta e il valore dell' offerta devono essere numeri 'interi'!");
			return;
		}

		try {
			// This return the Auction object related to the specified auction if it exists or null
			auction = auctionDAO.getAuction(aucId);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(500, "Errore, Asta non trovata nel database!");
			return;
		}

		if (auction != null) {

			// Checks if the logged user has created the auction
			if (auction.getCreator() == (user.getUser_id())) {
				response.sendError(400, "Errore, non e' concesso fare offerte sulle proprie aste!");
				return;
			}

			try {
				// This returns the Offer object related to the maximum offer for specified auction if it exists or null
				maxAuctionOffer = offerDAO.getWinningOffer(aucId);
			} catch (SQLException e) {
				e.printStackTrace();
				response.sendError(500, "Errore, accesso al database fallito, non trovo l'offerta massima per quell'asta!");
				return;
			}

			// This checks, if there is a maximum offer, if it belongs to the logged user
			// If so, the user is not allowed to make another offer
			if (maxAuctionOffer != null && user.getUser_id() == (maxAuctionOffer.getUser())) {
				response.sendError(400, "Errore, è necessario attendere che qualcun altro faccia un' offerta, prima di poterne fare una nuova!");
				return;
			}
			if (checkValue(offerValue, auction, maxAuctionOffer)) {
				try {
					// This returns true if the Offer has been added to the database
					offerDAO.insertOffer(offerValue, currLdt, user.getUser_id(), aucId);  // NON CONTROLLO LA BUONA RIUSCITA
				} catch (SQLException e) {
					e.printStackTrace();
					response.sendError(500, "Errore, accesso al database fallito!");
					return;
				}
			} else {
				response.sendError(400, "Errore, il valore specificato deve essere superiore all' offerta massima attuale di un ammontare"
						+ " pari almeno al rialzo minimo! Se non ci sono offerte al momento, il valore minimo deve essere pari"
						+ " o superiore al valore iniziale dell' asta! Il valore massimo � 2 miliardi!");
			}

			String path = "GetAuctionDetails?auctionId=" + strAucId + "&page=offerta.html";
			response.sendRedirect(path);

		} else {
			// The given Id doesn't belong to any of the auctions
			response.sendError(400, "Errore, l'asta specificata non esiste!");
		}
	}

}
