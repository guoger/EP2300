package peersim.EP2300.message;

import peersim.core.Node;

public class UpdateVectorAvg {
	final public Node sender;
	final public double level;
	final public double parent;
	final public long totalReqTimeInSubtree;
	final public long totalReqNumInSubtree;

	public UpdateVectorAvg(Node sender, double level, double parent,
			long totalReqTimeInSubtree, long totalReqNumInSubtree) {
		this.sender = sender;
		this.level = level;
		this.parent = parent;
		this.totalReqTimeInSubtree = totalReqTimeInSubtree;
		this.totalReqNumInSubtree = totalReqNumInSubtree;
	}
}
