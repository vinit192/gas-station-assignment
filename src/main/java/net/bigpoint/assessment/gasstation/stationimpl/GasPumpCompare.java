package net.bigpoint.assessment.gasstation.stationimpl;

import java.util.Comparator;

import net.bigpoint.assessment.gasstation.GasPump;

public class GasPumpCompare implements Comparator<GasPump>{

	@Override
	public int compare(GasPump o1, GasPump o2) {
		return (int)(o1.getRemainingAmount() - o2.getRemainingAmount());
	}

}
