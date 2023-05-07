package beans;

import java.time.LocalDateTime;

public class Offer {
	private int offer_id;
	private int price;
	private LocalDateTime time;
	private int user;
	private int auction;
	public int getOffer_id() {
		return offer_id;
	}
	public void setOffer_id(int offer_id) {
		this.offer_id = offer_id;
	}
	public int getPrice() {
		return price;
	}
	public void setPrice(int price) {
		this.price = price;
	}
	public LocalDateTime getTime() {
		return time;
	}
	public void setTime(LocalDateTime time) {
		this.time = time;
	}
	public int getUser() {
		return user;
	}
	public void setUser(int user) {
		this.user = user;
	}
	public int getAuction() {
		return auction;
	}
	public void setAuction(int auction) {
		this.auction = auction;
	}
}
