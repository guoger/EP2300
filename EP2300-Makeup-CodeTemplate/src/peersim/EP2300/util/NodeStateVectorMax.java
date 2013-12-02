package peersim.EP2300.util;

public class NodeStateVectorMax {
	/*
	 * Every variable here represent the status of subtree
	 */
	public String status;
	public double level;
	public long maxReqTime;

	public NodeStateVectorMax(String status, double level, long maxReqTime) {
		this.status = status;
		this.level = level;
		this.maxReqTime = maxReqTime;
	}
}
