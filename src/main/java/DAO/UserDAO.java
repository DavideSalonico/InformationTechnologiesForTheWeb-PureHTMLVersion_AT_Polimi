package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import beans.User;

public class UserDAO {
	private Connection connection;
	private PreparedStatement pstatement = null;
	private ResultSet result = null;

	public UserDAO(){}

	public UserDAO(Connection con) {
		this.connection = con;
	}
	
	public User checkCredentials(String username, String password) throws SQLException{
		User user = null;
		String query = "SELECT * FROM user WHERE username = ? AND password = ?";
		try {
			this.pstatement = connection.prepareStatement(query);
			// This sets the user_id as first parameter of the query
			pstatement.setString(1, username);
			pstatement.setString(1, password);
			result = pstatement.executeQuery();
			// If there is a match the entire row is returned here as a result
			if (!result.isBeforeFirst()) // no results, credential check failed
				return null;
			else {
				result.next();
				user = new User();
				user.setUser_id(result.getInt("user_id"));
				user.setUsername(result.getString("username"));
				user.setPassword(result.getString("password"));
				user.setAddress(result.getString("address"));
				return user;
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
	}
	
	
	
	public User getUser(int user_id) throws SQLException{
		User user = null;
		String query = "SELECT * FROM user WHERE user_id = ?";
		
		try {
			this.pstatement = connection.prepareStatement(query);
			// This sets the user_id as first parameter of the query
			pstatement.setInt(1, user_id);
			result = pstatement.executeQuery();
			// If there is a match the entire row is returned here as a result
			if(result.next()) {
				// Here a User object is initialized and the attributes obtained from the database are set
				user = new User();
				user.setUser_id(result.getInt("user_id"));
				user.setUsername(result.getString("username"));
				user.setPassword(result.getString("password"));
				user.setAddress(result.getString("address"));
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
		return user;
	}
}
