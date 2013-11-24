package peersim.EP2300.util;

public class NodeState {
	public String status;
	public double level;
	public float aggregate;

	public NodeState(String status, double level, float aggregate) {
		this.status = status;
		this.level = level;
		this.aggregate = aggregate;
	}
}
