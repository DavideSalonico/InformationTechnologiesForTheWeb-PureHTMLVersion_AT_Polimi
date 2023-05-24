package beans;

import java.time.LocalDateTime;

public class Auction {
	private int auction_id;
	private boolean open;
	private int initial_price;
	private int minimum_raise;
	private LocalDateTime expiring_date;
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
	public LocalDateTime getExpiring_date() {
		return expiring_date;
	}
	public void setExpiring_date(LocalDateTime expiring_date) {
		this.expiring_date = expiring_date;
	}
	public int getCreator() {
		return creator;
	}
	public void setCreator(int creator) {
		this.creator = creator;
	}
	public int getMinimum_raise() {
		return minimum_raise;
	}
	public void setMinimum_raise(int minimum_raise) {
		this.minimum_raise = minimum_raise;
	}
	
	
}
