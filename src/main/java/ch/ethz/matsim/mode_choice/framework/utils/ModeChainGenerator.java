package ch.ethz.matsim.mode_choice.framework.utils;

import java.util.Iterator;
import java.util.List;

public interface ModeChainGenerator extends Iterator<List<String>> {
	int getNumberOfAlternatives();
}