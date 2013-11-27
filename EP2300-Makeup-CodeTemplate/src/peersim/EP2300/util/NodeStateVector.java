package peersim.EP2300.util;

public class NodeStateVector {
	/*
	 * Every variable here represent the status of subtree
	 */
	public String status;
	public double level;
	public long totalReqTime;
	public long totalReqNum;
	public long maxReqTime;
	public long nodeNum;

	public NodeStateVector(String status, double level, long totalReqTime,
			long totalReqNum, long maxReqTime, long nodeNum) {
		this.status = status;
		this.level = level;
		this.totalReqTime = totalReqTime;
		this.totalReqNum = totalReqNum;
		this.maxReqTime = maxReqTime;
		this.nodeNum = nodeNum;
	}
}
