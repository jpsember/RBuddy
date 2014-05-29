package js.rbuddy;

import java.util.Date;
import static js.basic.Tools.*;

public class Receipt {

	/**
	 * Constructor
	 * 
	 * Sets date to current date
	 */
	public Receipt() {
	}

	public Date getDate() {
		return this.date;
	}

	public void setDate(Date date) {
		warning("Java's Date class has lots of problems; look for alternate?");
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String s) {
		s = s.trim();
		summary = s;
	}

	private Date date;
	private String summary;
}
