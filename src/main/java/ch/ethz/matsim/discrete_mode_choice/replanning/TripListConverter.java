package ch.ethz.matsim.discrete_mode_choice.replanning;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.router.MainModeIdentifier;
import org.matsim.core.router.StageActivityTypes;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.router.TripStructureUtils.Trip;
import org.matsim.core.utils.misc.Time;

import ch.ethz.matsim.discrete_mode_choice.model.DiscreteModeChoiceTrip;

/**
 * Helper class for converting a MATSim plan into a list of
 * DiscreteModeChoiceTrip.
 * 
 * @author sebhoerl
 */
public final class TripListConverter {
	private final static Logger logger = Logger.getLogger(TripListConverter.class);

	private TripListConverter() {

	}

	private static double getTripDuration(Trip trip, double startTime) {
		double time = startTime;

		for (PlanElement element : trip.getTripElements()) {
			if (element instanceof Activity) {
				Activity activity = (Activity) element;

				if (!Time.isUndefinedTime(activity.getEndTime())) {
					time = activity.getEndTime();
				} else if (!Time.isUndefinedTime(activity.getMaximumDuration())) {
					time += activity.getMaximumDuration();
				} else {
					return Time.getUndefinedTime();
				}
			} else {
				Leg leg = (Leg) element;

				if (!Time.isUndefinedTime(leg.getDepartureTime())) {
					time = leg.getDepartureTime();
				} else if (!Time.isUndefinedTime(leg.getTravelTime())) {
					time += leg.getTravelTime();
				} else {
					return Time.getUndefinedTime();
				}
			}
		}

		return Math.max(0.0, time - startTime);
	}

	private static int warningCount = 0;

	/**
	 * Convert a MATSim plan into a list of DiscreteModeChoiceTrip and extract the
	 * respective legs. It is expected that the plan is already flattened (i.e.
	 * there are no interaction activities).
	 */
	public static List<DiscreteModeChoiceTrip> convert(Plan plan, MainModeIdentifier mainModeIdentifier,
			StageActivityTypes stageActivityTypes) {
		List<Trip> initialTrips = TripStructureUtils.getTrips(plan, stageActivityTypes);
		List<DiscreteModeChoiceTrip> trips = new ArrayList<>(initialTrips.size());

		double time = 0.0;
		int index = 0;

		double tripDuration = Double.NaN;

		for (Trip initialTrip : initialTrips) {
			Activity originActivity = initialTrip.getOriginActivity();
			Activity destinationActivity = initialTrip.getDestinationActivity();

			Leg firstLeg = (Leg) initialTrip.getTripElements().get(0);
			String routingMode = mainModeIdentifier.identifyMainMode(initialTrip.getTripElements());

			if (!Time.isUndefinedTime(originActivity.getEndTime())) {
				time = originActivity.getEndTime();
			} else if (!Time.isUndefinedTime(firstLeg.getDepartureTime())) {
				time = firstLeg.getDepartureTime();
			} else if (!Time.isUndefinedTime(originActivity.getMaximumDuration())
					&& !Time.isUndefinedTime(tripDuration)) {
				time += originActivity.getMaximumDuration() + tripDuration;
			} else {
				if (warningCount++ < 100) {
					logger.warn(String.format("Found origin activity with invalid timing information in agent %s. "
							+ "Neither activity end time, nor leg departure time, nor activity duration are given. "
							+ "Falling back to %s. (Only 100 warnings of this type will be shown.)",
							plan.getPerson().getId().toString(), Time.writeTime(time)));
				}
			}

			trips.add(new DiscreteModeChoiceTrip(originActivity, destinationActivity, routingMode,
					initialTrip.getTripElements(), time, plan.getPerson().hashCode(), index));
			index++;

			tripDuration = getTripDuration(initialTrip, time);
		}

		return trips;
	}
}
