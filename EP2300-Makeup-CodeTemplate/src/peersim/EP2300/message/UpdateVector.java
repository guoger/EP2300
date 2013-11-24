package peersim.EP2300.message;

/*
 * message structure to be sent among nodes
 */
public class UpdateVector {
	private double id;
	private double level;
	private double parent;
	private float aggregate;

	public void set(double id, double level, double parent, float aggregate) {
		this.id = id;
		this.level = level;
		this.parent = parent;
		this.aggregate = aggregate;
	}

	public UpdateVector(double id, double level, double parent, float aggregate) {
		this.id = id;
		this.level = level;
		this.parent = parent;
		this.aggregate = aggregate;
	}
}
