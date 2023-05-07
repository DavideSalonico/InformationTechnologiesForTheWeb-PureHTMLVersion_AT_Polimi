package beans;

import java.time.LocalDateTime;

public class Auction {
	private int auction_id;
	private boolean open;
	private int initial_price;
	private LocalDateTime expiring_time;
	private int creator;
	
	public int getAuction_id() {
		return auction_id;
	}
	public void setAuction_id(int auction_id) {
		this.auction_id = auction_id;
	}
	public boolean isOpen() {
		return open;
	}
	public void setOpen(boolean open) {
		this.open = open;
	}
	public int getInitial_price() {
		return initial_price;
	}
	public void setInitial_price(int initial_price) {
		this.initial_price = initial_price;
	}
	public LocalDateTime getExpiring_time() {
		return expiring_time;
	}
	public void setExpiring_time(LocalDateTime expiring_time) {
		this.expiring_time = expiring_time;
	}
	public int getCreator() {
		return creator;
	}
	public void setCreator(int creator) {
		this.creator = creator;
	}
	
	
}
