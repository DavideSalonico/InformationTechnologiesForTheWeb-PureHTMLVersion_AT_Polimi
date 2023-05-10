package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import beans.Auction;

public class AuctionDAO {
	private Connection connection;
	PreparedStatement pstatement = null;
	ResultSet result = null;
	
	public AuctionDAO(Connection conn) {
		this.connection = conn;
	}
	
	public boolean isClosed(int auction_id) throws SQLException{
		try {
			pstatement = connection.prepareStatement("SELECT * FROM auction WHERE auction_id = ?");
			pstatement.setInt(1, auction_id);
			result = pstatement.executeQuery();
			if(result.getBoolean("open") == true) {
				return false;
			}
			return true;
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
	}
	
	public void insertAuction(int initial_price, LocalDateTime expiring_time, int minimum_raise, int creator) throws SQLException{
		try {
			pstatement = connection.prepareStatement("INSERT INTO auction (initia_price, expiring_time, minimum_raise, creator) VALUES(?, ?, ?, ?)");
			pstatement.setInt(1, initial_price);
			pstatement.setObject(2, expiring_time);
			pstatement.setInt(3, minimum_raise);
			pstatement.setInt(4, creator);
			pstatement.executeQuery();
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
	
	public List<Auction> search(String keyword) throws SQLException{
		List<Auction> auctions = new ArrayList<>();
		try {
			//TODO: read again where to look the similarity
			pstatement = connection.prepareStatement("SELECT * FROM auction au, article ar WHERE ar.auction_id = au.auction_id AND (ar.description LIKE ? OR ar.name LIKE ?)");
			pstatement.setString(1, "%" + keyword + "%");
			pstatement.setString(2, "%" + keyword + "%");
			result = pstatement.executeQuery();
			while(result.next()) {
				auctions.add(resultToAuction(result));
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
		return auctions;
	}
	
	public List<Auction> getOpenAuctions(int user_id) throws SQLException{
		List<Auction> auctions = new ArrayList<>();	
		try {
			pstatement = connection.prepareStatement("SELECT * FROM auction WHERE creator = ? AND open = '1'");
			pstatement.setInt(1, user_id);
			result = pstatement.executeQuery();
			if(result.next()) {
				auctions.add(resultToAuction(result));
			}
		} catch (SQLException e) {
		    e.printStackTrace();
			throw new SQLException(e);

		} finally {
			try {
				result.close();
			} catch (Exception e1) {
				throw new SQLException(e1);
			}
			try {
				pstatement.close();
			} catch (Exception e2) {
				throw new SQLException(e2);
			}
		}	
		return auctions;
	}
	
	public List<Auction> getClosedAuctions(int user_id) throws SQLException{
		List<Auction> auctions = new ArrayList<>();	
		try {
			pstatement = connection.prepareStatement("SELECT * FROM auction WHERE creator = ? AND open = '0'");
			pstatement.setInt(1, user_id);
			result = pstatement.executeQuery();
			if(result.next()) {
				auctions.add(resultToAuction(result));
			}
		} catch (SQLException e) {
		    e.printStackTrace();
			throw new SQLException(e);

		} finally {
			try {
				result.close();
			} catch (Exception e1) {
				throw new SQLException(e1);
			}
			try {
				pstatement.close();
			} catch (Exception e2) {
				throw new SQLException(e2);
			}
		}	
		return auctions;
	}
	
	public Auction getAuction(int auction_id) throws SQLException{
		Auction auction;
		try {
			pstatement = connection.prepareStatement("SELECT * FROM auction WHERE auction_id = ?");
			pstatement.setInt(1, auction_id);
			result = pstatement.executeQuery();
			auction = resultToAuction(result);
		} catch (SQLException e) {
		    e.printStackTrace();
			throw new SQLException(e);

		} finally {
			try {
				result.close();
			} catch (Exception e1) {
				throw new SQLException(e1);
			}
			try {
				pstatement.close();
			} catch (Exception e2) {
				throw new SQLException(e2);
			}
		}	
		return auction;
	}
	
	public LocalDateTime getExpiringTime(int auction_id) throws SQLException{
		LocalDateTime exp_time;
		try {
			pstatement = connection.prepareStatement("SELECT expiring_time FROM auction WHERE auction_id = ?");
			pstatement.setInt(1, auction_id);
			result = pstatement.executeQuery();
			exp_time = result.getTimestamp("expiring_time").toLocalDateTime();
		} catch (SQLException e) {
		    e.printStackTrace();
			throw new SQLException(e);

		} finally {
			try {
				result.close();
			} catch (Exception e1) {
				throw new SQLException(e1);
			}
			try {
				pstatement.close();
			} catch (Exception e2) {
				throw new SQLException(e2);
			}
		}	
		return exp_time;
	}
	
	public List<Auction> getValidAuctions(LocalDateTime session_datetime) throws SQLException{
		List<Auction> auctions = new ArrayList<>();	
		try {
			pstatement = connection.prepareStatement("SELECT * FROM auction WHERE time < ? AND open = 1");
			pstatement.setObject(1, session_datetime);
			result = pstatement.executeQuery();
			if(result.next()) {
				auctions.add(resultToAuction(result));
			}
		} catch (SQLException e) {
		    e.printStackTrace();
			throw new SQLException(e);

		} finally {
			try {
				result.close();
			} catch (Exception e1) {
				throw new SQLException(e1);
			}
			try {
				pstatement.close();
			} catch (Exception e2) {
				throw new SQLException(e2);
			}
		}	
		return auctions;
	}
	
	public boolean changeAuctionStatus(int auction_id) throws SQLException{
				int result = 0;		
				try {
					pstatement = connection.prepareStatement("UPDATE auction SET open = 0 WHERE auction_id = ?");
					pstatement.setInt(1, auction_id);
					result = pstatement.executeUpdate();
					// If there is an affected row, it means that the auction has been closed
					if(result > 0)
						return true;
				} catch (SQLException e) {
				    e.printStackTrace();
					throw new SQLException(e);
				} finally {
					try {
						pstatement.close();
					} catch (Exception e1) {}
				}		
				return false;
	}
	
	private Auction resultToAuction(ResultSet result) throws SQLException{
		Auction auction = new Auction();
		auction.setAuction_id(result.getInt("auction_id"));
		auction.setOpen(result.getBoolean("open"));
		auction.setCreator(result.getInt("creator"));
		auction.setInitial_price(result.getInt("initial_price"));
		auction.setMinimum_raise(result.getInt("minimum_raise"));
		auction.setExpiring_time(result.getTimestamp("expiring_time").toLocalDateTime());
		return auction;
	}
}
