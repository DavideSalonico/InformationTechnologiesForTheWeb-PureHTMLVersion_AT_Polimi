package utils;

import beans.Article;
import beans.Auction;
import beans.Offer;

import java.util.ArrayList;
import java.util.List;

public class AuctionFullInfo {
    private Auction auction;
    private List<Article> articles;
    private Offer maxOffer;

    public AuctionFullInfo(Auction auction, List<Article> articles, Offer maxOffer) {
        this.auction = auction;
        this.articles = new ArrayList<>();
        for(Article article : articles) {
            article.setImage(null);
            this.articles.add(article);
        }
        this.maxOffer = maxOffer;
    }

    public void addArticle(Article article) {
    	article.setImage(null);
    	this.articles.add(article);
    }

    public AuctionFullInfo(int auction_id, List<Article> articles, Offer maxOffer ){
        this.auction = new Auction();
        this.auction.setAuction_id(auction_id);
        this.articles = new ArrayList<>();
        for(Article article : articles) {
            article.setImage(null);
            this.articles.add(article);
        }
        this.maxOffer = maxOffer;
    }

    public Auction getAuction() {
        return auction;
    }

    public Offer getMaxOffer() {
        return maxOffer;
    }

    public List<Article> getArticles() {
        return articles;
    }
}
