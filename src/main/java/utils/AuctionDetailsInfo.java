package utils;

import beans.Article;
import beans.Auction;
import beans.Offer;
import beans.User;

import java.util.ArrayList;
import java.util.List;

public class AuctionDetailsInfo {
    private Auction auction;
    private List<Article> articles = new ArrayList<>();
    private List<Pair<Offer, String>> offers_username = new ArrayList<>();
    private User winner;

    public AuctionDetailsInfo(Auction auction, List<Article> articles, List <Pair<Offer,String>> offers_username, User winner) {
        this.auction = auction;
        this.articles = new ArrayList<>(articles);
    }

    public void addWinner(User winner) {
    	winner.setUser_id(0); //Not to show user_id to the client
        this.winner = winner;
    }

    public void addOfferWinner(List<Pair<Offer,String>> copy, User awardedUser) {
    	this.offers_username = new ArrayList<>(copy);
        for(Pair<Offer,String> pair : offers_username){
            pair.getFirst().setUser(0); //Not to show user_id to the client
        }
        if(!auction.isOpen()){
            addWinner(awardedUser);
        }
    }

    public List<Article> getArticles() {
        return articles;
    }

    public Offer getMaxAuctionOffer() {
        if(offers_username.size() == 0) return null;
        return offers_username.get(0).getFirst();
    }

    public List<Pair<Offer, String>> getAuctionOffers() {
        return this.offers_username;
    }

    public User getWinner() {
        return winner;
    }
}
