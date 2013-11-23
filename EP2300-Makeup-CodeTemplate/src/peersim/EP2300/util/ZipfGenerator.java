/**
 * The following code is a Zipf generator obtained from "http://diveintodata.org/2009/09/zipf-distribution-generator-in-java/"
 * 
 */

package peersim.EP2300.util;

import java.util.Random;

import peersim.core.CommonState;

public class ZipfGenerator {
	private Random rnd;// = new Random(System.currentTimeMillis());
	private int size;
	private double skew;
	private double bottom = 0;

	public ZipfGenerator(int size, double skew) {
		this.size = size;
		this.skew = skew;
		this.rnd = CommonState.r;

		this.bottom = 1;
	}

	// the next() method returns an rank id. The frequency of returned rank ids
	// are follows Zipf distribution.
	public int next() {
		int rank;
		double friquency = 0;
		double dice;

		rank = rnd.nextInt(size);
		friquency = (1.0d / Math.pow(rank, this.skew)) / this.bottom;
		dice = rnd.nextDouble();

		while (!(dice < friquency)) {
			rank = rnd.nextInt(size);
			friquency = (1.0d / Math.pow(rank, this.skew)) / this.bottom;
			dice = rnd.nextDouble();
		}

		return rank;
	}

	// This method returns a probability that the given rank occurs.
	public double getProbability(int rank) {
		// return (1.0d / Math.pow(rank, this.skew)) / this.bottom;
		return (1.0d / Math.pow(rank, this.skew));
	}

}