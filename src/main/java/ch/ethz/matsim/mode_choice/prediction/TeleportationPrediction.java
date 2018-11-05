package ch.ethz.matsim.mode_choice.prediction;

public class TeleportationPrediction {
	final public double travelTime;
	final public double distance;
	
	public TeleportationPrediction(double travelTime, double distance) {
		this.travelTime = travelTime;
		this.distance = distance;
	}
}