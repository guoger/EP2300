package peersim.EP2300.message;

/*
 * message structure to be sent among nodes
 */
public class UpdateVector {
	final public double id;
	final public double level;
	final public double parent;
	final public long aggregate;

	public UpdateVector(double id, double level, double parent, long aggregate) {
		this.id = id;
		this.level = level;
		this.parent = parent;
		this.aggregate = aggregate;
	}
}
