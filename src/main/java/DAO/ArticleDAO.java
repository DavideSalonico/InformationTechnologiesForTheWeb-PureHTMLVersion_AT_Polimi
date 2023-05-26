package DAO;

import beans.Article;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArticleDAO {
	private Connection connection;
	private PreparedStatement pstatement = null;
	private ResultSet result = null;

	public ArticleDAO(Connection connection) {
		this.connection = connection;
	}
	
	public boolean isSold(int article_id) throws SQLException{
		boolean isSold = false;
		String query = "SELECT sold FROM article WHERE article_id = ?";
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, article_id);
			ResultSet result = pstatement.executeQuery();
			if(result.next())
				isSold = result.getBoolean("sold");  //BIT type automatically converted to boolean
		}catch(SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}finally {
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
		
		return isSold;
	}
	
	public void insertArticle(String name, String description, String image, int price, int user_id) throws SQLException{
		String query = "INSERT into article (name, description, image, price, sold, user_id) VALUES (?,?,?,?,false,?)";
		try{
			pstatement  = connection.prepareStatement(query);
			pstatement.setString(1,name);
			pstatement.setString(2,description);
			pstatement.setString(3, image);
			pstatement.setInt(4, price);
			pstatement.setInt(5, user_id);
			pstatement.executeUpdate();
		}catch(SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}finally {
			try {
				pstatement.close();
			} catch (Exception e2) {
				throw new SQLException(e2);
			}
		}
	}
	
	public void addToAuction(int article_id, int auction_id) throws SQLException{
		String query = "UPDATE article SET auction_id = ? where article_id = ?";
		try{
			pstatement  = connection.prepareStatement(query);
			pstatement.setInt(1,auction_id);
			pstatement.setInt(2,article_id);
			pstatement.executeUpdate();
		}catch(SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}finally {
			try {
				pstatement.close();
			} catch (Exception e2) {
				throw new SQLException(e2);
			}
		}
	}
	
	public List<Article> getAuctionArticles(int auction_id) throws SQLException{
		List<Article> articles = new ArrayList<Article>();
		Article article = new Article();
		String query = "SELECT * FROM article WHERE auction_id = ?";
		try{
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, auction_id);
			result = pstatement.executeQuery();
			while (result.next()) {
				article = resultToArticle(result);
				articles.add(article);
			}
		}catch(SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}finally {
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
		return articles;
	}
	
	public List<Article> getUserArticles(int user_id) throws SQLException{
		List<Article> articles = new ArrayList<Article>();
		Article article = new Article();
		String query = "SELECT * FROM article WHERE article_creator = ?";
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, user_id);
			result = pstatement.executeQuery();
			while (result.next()) {
				article = resultToArticle(result);
				articles.add(article);
			}
		}catch(SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}finally {
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
		return articles;
	}
	
	public Article getArticle(int article_id) throws SQLException{
		Article article = new Article(); 
		String query = "SELECT * FROM article WHERE article_id = ?";
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, article_id);
			result = pstatement.executeQuery();
			while (result.next()) {
				article = resultToArticle(result);
			}
		}catch(SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}finally {
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
		return article;
	}
	
	public Article resultToArticle(ResultSet result) throws SQLException{
		Article article = new Article(); 
		article.setArticle_id(result.getInt("article_id"));
		article.setName(result.getString("name"));
		article.setDescription(result.getString("description"));
		article.setImage(result.getBlob("image"));
		article.setArticle_creator(result.getInt("article_creator"));
		article.setAuction_id(result.getInt("auction_id"));
		article.setPrice(result.getInt("price"));
		article.setSold(result.getBoolean("sold"));
		
		return article;
	}
}
