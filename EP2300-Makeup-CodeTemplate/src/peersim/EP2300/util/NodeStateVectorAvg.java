package peersim.EP2300.util;

public class NodeStateVectorAvg {
	public String status;
	public double level;
	public long totalReqTime;
	public long totalReqNum;

	public NodeStateVectorAvg(String status, double level, long totalReqTime,
			long totalReqNum) {
		this.status = status;
		this.level = level;
		this.totalReqTime = totalReqTime;
		this.totalReqNum = totalReqNum;
	}
}
