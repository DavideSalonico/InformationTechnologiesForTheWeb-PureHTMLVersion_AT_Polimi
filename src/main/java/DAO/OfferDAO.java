package DAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import beans.Auction;
import beans.Offer;

public class OfferDAO {
	private Connection connection;
	
	public OfferDAO(Connection conn) {
		this.connection = conn;
	}
	
	public List<Offer> getOffers(int auction_id){
		return null;
	}
	
	public Auction getWinningOffer(int auction_id) throws SQLException{
		return null;
	}
	
	public void insertOffer(int price, LocalDateTime datetime, int user_id, int auction_id) throws SQLException{
		return;
	}
}
