package communication;

import java.io.Serializable;

public class Message implements Serializable {

	private static final long serialVersionUID = 1L;
	
	double lattitude;
	double longitude;
	double vector_x;
	double vector_y;
	String sender;

	public Message(String sender, double lat, double lon, double vecx, double vecy) {
		this.lattitude = lat;
		this.longitude = lon;
		this.vector_x = vecx;
		this.vector_y = vecy;
		this.sender = sender;
	}

	public void print() {
		System.out.println("------------------------------");
		System.out.printf("Coordinates: %f,%f\n",this.lattitude, this.longitude);
		System.out.printf("Path: <%f,%f>\n", this.vector_x, this.vector_y);
		System.out.println("Message from " + sender);
		System.out.println("------------------------------");
	}

	public Message clone() {
		Message clone = new Message(this.sender, this.lattitude, this.longitude, this.vector_x, this.vector_y);
		return clone;
	}
	
	
	
	
	
	/**
	 * @return the lattitude
	 */
	public double getLattitude() {
		return lattitude;
	}

	/**
	 * @param lattitude the lattitude to set
	 */
	public void setLattitude(double lattitude) {
		this.lattitude = lattitude;
	}

	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	/**
	 * @return the vector_x
	 */
	public double getVector_x() {
		return vector_x;
	}

	/**
	 * @param vector_x the vector_x to set
	 */
	public void setVector_x(double vector_x) {
		this.vector_x = vector_x;
	}

	/**
	 * @return the vector_y
	 */
	public double getVector_y() {
		return vector_y;
	}

	/**
	 * @param vector_y the vector_y to set
	 */
	public void setVector_y(double vector_y) {
		this.vector_y = vector_y;
	}

	/**
	 * @return the sender
	 */
	public String getSender() {
		return sender;
	}

	/**
	 * @param sender the sender to set
	 */
	public void setSender(String sender) {
		this.sender = sender;
	}
	
}
