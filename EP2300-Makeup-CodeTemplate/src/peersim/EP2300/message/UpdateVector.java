package peersim.EP2300.message;

import peersim.core.Node;

/*
 * message structure to be sent among nodes
 */
public class UpdateVector {
	final public Node sender;
	final public double level;
	final public double parent;
	final public long aggregate;

	public UpdateVector(Node sender, double level, double parent, long aggregate) {
		this.sender = sender;
		this.level = level;
		this.parent = parent;
		this.aggregate = aggregate;
	}
}
