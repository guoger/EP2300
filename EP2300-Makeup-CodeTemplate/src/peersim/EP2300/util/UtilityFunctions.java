package peersim.EP2300.util;

import peersim.core.CommonState;

/**
 * Helper class for utility functions
 * @author Rerngvit Yanggratoke (rerngvit@kth.se)
 *
 */
public class UtilityFunctions {

	public static double sampleExponentialDistribution(double lambda) {
		double random = CommonState.r.nextDouble();

		double x = Math.log(1 - random) / (-lambda);

		return x;

	}

}
