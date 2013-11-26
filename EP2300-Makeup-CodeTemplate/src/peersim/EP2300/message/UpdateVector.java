package peersim.EP2300.message;

import peersim.core.Node;

/*
 * message structure to be sent among nodes
 */
public class UpdateVector {
	final public Node sender;
	final public double level;
	final public double parent;
	final public long totalReqTimeInSubtree;
	final public long totalReqNumInSubtree;
	final public long maxReqTimeInSubtree;

	public UpdateVector(Node sender, double level, double parent,
			long totalReqTimeInSubtree, long totalReqNumInSubtree,
			long maxReqTimeInSubtree) {
		this.sender = sender;
		this.level = level;
		this.parent = parent;
		this.totalReqTimeInSubtree = totalReqTimeInSubtree;
		this.totalReqNumInSubtree = totalReqNumInSubtree;
		this.maxReqTimeInSubtree = maxReqTimeInSubtree;
	}
}
