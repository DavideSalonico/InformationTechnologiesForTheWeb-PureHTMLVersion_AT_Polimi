package controllers;

import DAO.AuctionDAO;
import beans.User;
import utils.ConnectionHandler;

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


@WebServlet("/CloseAuction")
public class CloseAuction extends HttpServlet {
	@Serial
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private AuctionDAO auctionDAO;

	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();

		connection = ConnectionHandler.getConnection(servletContext);
		auctionDAO = new AuctionDAO(connection);
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int auction_id;

		try {
			auction_id = Integer.parseInt(request.getParameter("auctionId"));
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect auctionId value");
			return;
		}

		int user_id = ((User) request.getSession().getAttribute("user")).getUser_id();

		try {
			if (!auctionDAO.checkUserId(auction_id, user_id)) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Not possible to close this auction: you are not the owner");
				return;
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to check the user id. Try again later");
			return;
		}

		try {
			auctionDAO.changeAuctionStatus(auction_id);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to close the auction. Try again later");
		}

		String path = getServletContext().getContextPath() + "/GoToSell";
		response.sendRedirect(path);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
