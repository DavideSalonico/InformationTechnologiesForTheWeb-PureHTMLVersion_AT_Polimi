package DAO;

import beans.Offer;

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
			pstatement = connection.prepareStatement("SELECT * FROM offer WHERE auction = ?");
			pstatement.setInt(1, auction_id);
			result = pstatement.executeQuery();
			while(result.next()) {
				Offer off = new Offer();
				off.setOffer_id(result.getInt("offer_id"));
				off.setPrice(result.getInt("price"));
				off.setTime(result.getTimestamp("time").toLocalDateTime());
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
				off.setTime(result.getTimestamp("time").toLocalDateTime());
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
			//Assumo che l'ultima offerta in ordine di data con il corretto auction_id sia effettivamente la vincente
			pstatement = connection.prepareStatement("SELECT * FROM offer WHERE time = (SELECT MAX(time) FROM offer WHERE auction = ?)");
			pstatement.setInt(1, auction_id);
			result = pstatement.executeQuery();
			if(result.next()) {
				off = new Offer();
				off.setOffer_id(result.getInt("offer_id"));
				off.setPrice(result.getInt("price"));
				off.setTime(result.getTimestamp("time").toLocalDateTime());
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
	
	public void insertOffer(int price, LocalDateTime datetime, int user_id, int auction_id) throws SQLException{
		//TODO: controls can be added
		try {
			pstatement = connection.prepareStatement("INSERT INTO offer (auction, user, price, time) VALUES(?, ?, ?, ?)");
			pstatement.setInt(1, auction_id);
			pstatement.setInt(2, user_id);
			pstatement.setInt(3, price);
			pstatement.setObject(4, datetime);
			pstatement.executeUpdate();
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
}
