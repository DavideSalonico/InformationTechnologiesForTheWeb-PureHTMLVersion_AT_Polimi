package DAO;

import beans.Offer;
import utils.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OfferDAO {
	private Connection connection;
	private PreparedStatement pstatement = null;
	private ResultSet result = null;
	
	public OfferDAO(Connection conn) {
		this.connection = conn;
	}
	
	public List<Offer> getOffers(int auction_id) throws SQLException{
		List<Offer> offers = new ArrayList<Offer>();
		try {
			pstatement = connection.prepareStatement("SELECT * FROM offer WHERE auction = ? ORDER BY price DESC");
			pstatement.setInt(1, auction_id);
			result = pstatement.executeQuery();
			while(result.next()) {
				Offer off = new Offer();
				off.setOffer_id(result.getInt("offer_id"));
				off.setPrice(result.getInt("price"));
				off.setTime(result.getObject("time", LocalDateTime.class));
				off.setUser(result.getInt("user"));
				off.setAuction(result.getInt("auction"));
				offers.add(off);
			}	
		} catch(SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		} finally {
			try {
				result.close();
			} catch(Exception e1) {
				throw new SQLException(e1);
			}
			try {
				pstatement.close();
			} catch(Exception e2) {
				throw new SQLException(e2);
			}
		}	
		return offers;
	}
	
	public Offer getOffer(int offer_id) throws SQLException{
		Offer off = null;
		try {
			pstatement = connection.prepareStatement("SELECT * FROM offer WHERE offer_id = ?");
			pstatement.setInt(1, offer_id);
			result = pstatement.executeQuery();
			if (result.next()){
				off = new Offer();
				off.setOffer_id(result.getInt("offer_id"));
				off.setPrice(result.getInt("price"));
				off.setTime(result.getObject("time", LocalDateTime.class));
				off.setUser(result.getInt("user"));
				off.setAuction(result.getInt("auction"));
			}
		} catch(SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		} finally {
			try {
				result.close();
			} catch(Exception e1) {
				throw new SQLException(e1);
			}
			try {
				pstatement.close();
			} catch(Exception e2) {
				throw new SQLException(e2);
			}
		}	
		return off;
	}
	
	public Offer getWinningOffer(int auction_id) throws SQLException{
		Offer off = null;
		try {
			pstatement = connection.prepareStatement("SELECT * FROM offer WHERE price = (SELECT MAX(price) FROM offer WHERE auction = ?)");
			pstatement.setInt(1, auction_id);
			result = pstatement.executeQuery();
			if(result.next()) {
				off = new Offer();
				off.setOffer_id(result.getInt("offer_id"));
				off.setPrice(result.getInt("price"));
				off.setTime(result.getObject("time", LocalDateTime.class));
				off.setUser(result.getInt("user"));
				off.setAuction(result.getInt("auction"));	
			}
		} catch(SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		} finally {
			try {
				if(result == null) {
					result.close();
				}
			} catch(Exception e1) {
				throw new SQLException(e1);
			}
			try {
				pstatement.close();
			} catch(Exception e2) {
				throw new SQLException(e2);
			}
		}	
		return off;
	}
	
	public int insertOffer(int price, LocalDateTime datetime, int user_id, int auction_id) throws SQLException{
		int outcome = 0;
		try {
			pstatement = connection.prepareStatement("INSERT INTO offer (auction, user, price, time) VALUES(?, ?, ?, ?)");
			pstatement.setInt(1, auction_id);
			pstatement.setInt(2, user_id);
			pstatement.setInt(3, price);
			pstatement.setObject(4, datetime);
			outcome = pstatement.executeUpdate();
			if(outcome != 1) {
				throw new SQLException("Error in inserting offer");
			}
		} catch(SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		} finally {
			try {
				pstatement.close();
			} catch(Exception e2) {
				throw new SQLException(e2);
			}
		}
		return outcome;
	}

    public Map<Integer, Offer> getWinningOfferByUser(int userId) throws SQLException{
		Map<Integer, Offer> aucOff = new HashMap<Integer, Offer>();
		ResultSet rs = null;
		try{
			pstatement = connection.prepareStatement("SELECT o1.offer_id, o1.auction FROM offer o1 WHERE price = (SELECT MAX(price) FROM offer o2 WHERE o1.auction = o2.auction) AND o1.user = ?");
			pstatement.setInt(1, userId);
			rs = pstatement.executeQuery();
			while(rs.next()){
				aucOff.put(rs.getInt("auction"), this.getOffer(rs.getInt("offer_id")));
			}
		} catch(SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		} finally {
			try {
				if(rs != null)
					rs.close();
			} catch(Exception e1) {
				throw new SQLException(e1);
			}
			try {
				pstatement.close();
			} catch(Exception e2) {
				throw new SQLException(e2);
			}
		}	
		return aucOff;
	}

	public List<Pair<Offer,String>> getOffersUsername(int auction_id) throws SQLException{
		List<Pair<Offer,String>> offers = new ArrayList<>();
		try {
			pstatement = connection.prepareStatement("SELECT * FROM offer JOIN user on user_id = user WHERE auction = ? ORDER BY price DESC");
			pstatement.setInt(1, auction_id);
			result = pstatement.executeQuery();
			while(result.next()) {
				Offer off = new Offer();
				off.setOffer_id(result.getInt("offer_id"));
				off.setPrice(result.getInt("price"));
				off.setTime(result.getObject("time", LocalDateTime.class));
				//SO THE CLIENT CAN'T SEE THE USER ID
				off.setUser(result.getInt("user"));
				off.setAuction(result.getInt("auction"));
				String username = result.getString("username");
				offers.add(new Pair<>(off,username));
			}
		} catch(SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		} finally {
			try {
				result.close();
			} catch(Exception e1) {
				throw new SQLException(e1);
			}
			try {
				pstatement.close();
			} catch(Exception e2) {
				throw new SQLException(e2);
			}
		}
		return offers;
	}
}
