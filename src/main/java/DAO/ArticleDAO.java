package DAO;

import beans.Article;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ArticleDAO {
	private Connection connection;
	private PreparedStatement pstatement = null;
	private ResultSet result = null;

	public ArticleDAO(Connection connection) {
		this.connection = connection;
	}


	public void insertArticle(String name, String description, int price, int user_id, InputStream image) throws SQLException{
		String query = "INSERT into article (name, description, price, article_creator, image) VALUES (?,?,?,?,?)";
		int outcome;
		try{
			pstatement  = connection.prepareStatement(query);
			pstatement.setString(1,name.toUpperCase());
			pstatement.setString(2,description.toUpperCase());
			pstatement.setInt(3, price);
			pstatement.setInt(4, user_id);
			pstatement.setBlob(5, image);
			outcome = pstatement.executeUpdate();
			if(outcome == 0) {
				throw new SQLException("Creating article failed, no rows affected.");
			}
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
		int outcome;
		try{
			pstatement  = connection.prepareStatement(query);
			pstatement.setInt(1,auction_id);
			pstatement.setInt(2,article_id);
			outcome = pstatement.executeUpdate();
			if(outcome == 0) {
				throw new SQLException("Creating article failed, no rows affected.");
			}
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
	

	
	public List<Article> getAvailableUserArticles(int user_id) throws SQLException{
		List<Article> articles = new ArrayList<Article>();
		Article article;
		String query = "SELECT * FROM article WHERE article_creator = ? and auction_id  is null";
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

	public int getAuctionInitialPrice(int auction_id) throws SQLException{
		String query = "SELECT SUM(price) AS total FROM article where auction_id = ?";
		int initialPrice = 0;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, auction_id);
			result = pstatement.executeQuery();
			if (result.next()) {
				initialPrice = result.getInt("total");
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
		return initialPrice;
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
		
		return article;
	}
}
