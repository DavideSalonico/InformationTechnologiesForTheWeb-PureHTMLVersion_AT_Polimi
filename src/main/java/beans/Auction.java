package beans;

import java.time.LocalDateTime;

public class Auction {
	private int auction_id;
	private boolean open;
	private int initial_price;
	private LocalDateTime expiring_time;
	private int creator;
	
	public int getAuctionId() {
		return this.auction_id;
	}
	
	public void setAuctionId(int auctionId) {
		this.auction_id = auctionId;
	}
	
	public boolean getOpen() {
		return this.open;
	}
	
	public void setOpen(boolean open) {
		this.open = open;
	}
	
	public int getInitialPrice() {
		return this.initial_price;
	}
	
	public void setInitialPrice(int initialPrice) {
		this.initial_price = initialPrice;
	}
	
	public LocalDateTime getExpiringTime() {
		return this.expiring_time;
	}
	
	public void setExpiringTime(LocalDateTime expiringTime) {
		this.expiring_time = expiringTime;
	}
	
	public int getCreator() {
		return this.creator;
	}
	
	public void setCreator(int creator) {
		this.creator = creator;
	}
}
