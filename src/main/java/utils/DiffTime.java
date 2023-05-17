package utils;

import java.time.Duration;
import java.time.LocalDateTime;

// This object is used to represent the difference between 2 LocalDatetimes
public class DiffTime {
	private int days;
	private int hours;
	private int minutes;
	
	public int getDays() {
		return days;
	}
	public void setDays(int days) {
		this.days = days;
	}
	public int getHours() {
		return hours;
	}
	public void setHours(int hours) {
		this.hours = hours;
	}
	public int getMinutes() {
		return minutes;
	}
	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}
	
    public static DiffTime getRemainingTime(LocalDateTime requestLdt, LocalDateTime deadline)
    {
    	DiffTime dfTime = new DiffTime();
    	int remainder = 0;
    	// If the following condition is true, it means the auction is already expired
    	if(requestLdt.isAfter(deadline))
    	{
    		dfTime.setDays(0);
    		dfTime.setHours(0);
    		dfTime.setMinutes(0);
    	}
    	// If the auction is not expired yet calculates the remaining time
    	else
    	{
        	int diffSeconds = (int) Duration.between(requestLdt,  deadline).toSeconds();
        	dfTime.setDays(diffSeconds / 86400);
        	remainder = diffSeconds % 86400;
        	dfTime.setHours(remainder / 3600);
        	remainder = remainder % 3600;
        	dfTime.setMinutes(remainder / 60);
    	}
    	// Returns the diffTime object
    	return dfTime;
    }
}