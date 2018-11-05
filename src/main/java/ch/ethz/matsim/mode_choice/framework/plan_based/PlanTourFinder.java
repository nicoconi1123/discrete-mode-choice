package ch.ethz.matsim.mode_choice.framework.plan_based;

import java.util.Collections;
import java.util.List;

import ch.ethz.matsim.mode_choice.framework.ModeChoiceTrip;
import ch.ethz.matsim.mode_choice.framework.tour_based.TourFinder;

public class PlanTourFinder implements TourFinder {
	@Override
	public List<List<ModeChoiceTrip>> findTours(List<ModeChoiceTrip> trips) {
		return Collections.singletonList(trips);
	}
}