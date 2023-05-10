package DAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import beans.Article;

public class ArticleDAO {
	private Connection connection;

	public ArticleDAO(Connection connection) {
		this.connection = connection;
	}
	
	public boolean isSold(int article_id) throws SQLException{
		return false;
	}
	
	public void insertArticle(String name, String description, String image, int price, boolean sold, int user_id) throws SQLException{
		return;
	}
	
	public void addToAuction(int auction_id) throws SQLException{
		return;
	}
	
	public List<Article> getAuctionArticles(int user_id) throws SQLException{
		return null;
	}
	
	public List<Article> getUserArticles(int user_id) throws SQLException{
		return null;
	}
	
	public Article getArticle(int article_id) throws SQLException{
		return null;
	}
}
