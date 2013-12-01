package peersim.EP2300.util;

public class NodeStateVector {
	/*
	 * Every variable here represent the status of subtree
	 */
	public String status;
	public double level;
	public long maxReqTime;
	public long nodeNum;

	public NodeStateVector(String status, double level, long maxReqTime,
			long nodeNum) {
		this.status = status;
		this.level = level;
		this.maxReqTime = maxReqTime;
		this.nodeNum = nodeNum;
	}
}
