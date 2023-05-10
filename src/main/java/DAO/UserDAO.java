package DAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import beans.User;

public class UserDAO {
	private Connection connection;

	public UserDAO(){}

	public UserDAO(Connection con) {
		this.connection = con;
	}
	
	public boolean checkCredentials(String username, String password) throws SQLException{
		return false;
	}
	
	public List<String> getWinnerData(int user_id) throws SQLException{
		return null;
	}
	
	public User getUser(int user_id) throws SQLException{
		return null;
	}
}
