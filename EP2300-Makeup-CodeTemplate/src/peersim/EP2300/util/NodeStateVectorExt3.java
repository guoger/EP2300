package peersim.EP2300.util;

public class NodeStateVectorExt3 {
	public String status;
	public double level;
	public long totalReqTime;
	public long totalReqNum;
	public long reqRate;

	public NodeStateVectorExt3(String status, double level, long totalReqTime,
			long totalReqNum, long reqRate) {
		this.status = status;
		this.level = level;
		this.totalReqTime = totalReqTime;
		this.totalReqNum = totalReqNum;
		this.reqRate = reqRate;
	}
}
