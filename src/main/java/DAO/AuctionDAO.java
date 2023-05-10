package DAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import beans.Auction;

public class AuctionDAO {
	private Connection connection;
	
	public AuctionDAO() {};
	
	public AuctionDAO(Connection conn) {
		this.connection = conn;
	}
	
	public boolean isClosed(int auction_id) throws SQLException{
		return false;
	}
	
	public void insertAuction(int initial_price, LocalDateTime expirng_time, int minimum_raise, int creator) throws SQLException{
		return;
	}
	
	public List<Auction> search(String keyword) throws SQLException{
		return null;
	}
	
	public List<Auction> getOpenAuctions(int user_id) throws SQLException{
		return null;
	}
	
	public List<Auction> getClosedAuctions(int user_id) throws SQLException{
		return null;
	}
	
	public Auction getAuction(int aution_id) throws SQLException{
		return null;
	}
	
	public LocalDateTime getExpiringTime(int auction_id) throws SQLException{
		return null;
	}
	
	public List<Auction> getValidAuctions(LocalDateTime session_datetime) throws SQLException{
		return null;
	}
	
	public void changeAuctionStatus(int auction_id) throws SQLException{
		return;
	}
}
