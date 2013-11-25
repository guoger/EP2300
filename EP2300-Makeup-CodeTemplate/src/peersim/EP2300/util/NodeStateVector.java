package peersim.EP2300.util;

public class NodeStateVector {
	public String status;
	public double level;
	public long aggregate;

	public NodeStateVector(String status, double level, long aggregate) {
		this.status = status;
		this.level = level;
		this.aggregate = aggregate;
	}
}
