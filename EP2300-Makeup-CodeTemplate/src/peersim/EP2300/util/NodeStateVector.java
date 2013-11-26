package peersim.EP2300.util;

public class NodeStateVector {
	public String status;
	public double level;
	public long totalReqTime;
	public long totalReqNum;
	public long maxReqTime;

	public NodeStateVector(String status, double level, long totalReqTime,
			long totalReqNum, long maxReqTime) {
		this.status = status;
		this.level = level;
		this.totalReqTime = totalReqTime;
		this.totalReqNum = totalReqNum;
		this.maxReqTime = maxReqTime;
	}
}
