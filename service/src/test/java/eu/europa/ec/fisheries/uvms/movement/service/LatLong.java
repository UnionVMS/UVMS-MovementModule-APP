package eu.europa.ec.fisheries.uvms.movement.service;


import java.time.Instant;

public class LatLong {
	
	public double latitude;
	public double longitude;
	public Instant positionTime;
	public double bearing = Double.MIN_NORMAL;
	public double distance = 0;
	public double speed = 0;

	public LatLong(double latitude, double longitude, Instant positionTime) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
		this.positionTime = positionTime;
	}
	@Override
	public String toString() {
		
		String formatStr = "%2.6f";
		String la = String.format(formatStr , latitude) ;
		String lo = String.format(formatStr , longitude) ;
		String be = String.format(formatStr , bearing) ;

		return "[lat=" + la + ", lon=" + lo + ", pos=" + positionTime + ", bearing="+be + ", distance="+distance + ", speed="+speed+"]";
	}
}
