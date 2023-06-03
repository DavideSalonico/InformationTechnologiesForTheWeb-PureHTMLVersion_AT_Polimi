package beans;

import java.sql.Blob;

public class Article {
	private int article_id;
	private String name;
	private String description;
	private Blob image;
	private int article_creator;
	private int auction_id;
	private int price;
	
	public int getArticleId() {
		return this.article_id;
	}

	public int getArticle_id() {
		return article_id;
	}

	public void setArticle_id(int article_id) {
		this.article_id = article_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Blob getImage() {
		return image;
	}

	public void setImage(Blob image) {
		this.image = image;
	}

	public int getArticle_creator() {
		return article_creator;
	}

	public void setArticle_creator(int article_creator) {
		this.article_creator = article_creator;
	}

	public int getAuction_id() {
		return auction_id;
	}

	public void setAuction_id(int auction_id) {
		this.auction_id = auction_id;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	@Override
	public boolean equals (Object x){
		return this.article_id == ((Article) x).getArticle_id();
	}
}
