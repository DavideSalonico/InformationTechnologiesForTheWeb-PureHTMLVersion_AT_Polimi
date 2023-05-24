package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import beans.Offer;

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
			pstatement = connection.prepareStatement("SELECT * FROM offer WHERE auction_id = ?");
			pstatement.setInt(1, auction_id);
			result = pstatement.executeQuery();
			while(result.next()) {
				Offer off = new Offer();
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
		return offers;
	}
	
	public Offer getOffer(int offer_id) throws SQLException{
		Offer offers = null;
		try {
			pstatement = connection.prepareStatement("SELECT * FROM offer WHERE offer_id = ?");
			pstatement.setInt(1, offer_id);
			result = pstatement.executeQuery();
			result.next();
			Offer off = new Offer();
			off.setOffer_id(result.getInt("offer_id"));
			off.setPrice(result.getInt("price"));
			off.setTime(result.getTimestamp("time").toLocalDateTime());
			off.setUser(result.getInt("user"));
			off.setAuction(result.getInt("auction"));	
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
	
	public Offer getWinningOffer(int auction_id) throws SQLException{
		Offer off = new Offer();
		try {
			//Assumo che l'ultima offerta in ordine di data con il corretto auction_id sia effettivamente la vincente
			pstatement = connection.prepareStatement("SELECT * FROM offer WHERE time = (SELECT MIN(time) FROM offer WHERE auction = ?)");
			pstatement.setInt(1, auction_id);
			result = pstatement.executeQuery();
			if(result.next()) {
				off.setOffer_id(result.getInt("offer_id"));
				off.setPrice(result.getInt("price"));
				off.setTime(result.getTimestamp("time").toLocalDateTime());
				off.setUser(result.getInt("user"));
				off.setAuction(result.getInt("auction"));	
			};
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
			pstatement = connection.prepareStatement("INSERT INTO offer (auction_id, user_id, price, time) VALUES(?, ?, ?, ?)");
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
		return;
	}
}
