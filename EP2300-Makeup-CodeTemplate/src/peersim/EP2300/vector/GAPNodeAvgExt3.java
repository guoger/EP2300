package peersim.EP2300.vector;

public class GAPNodeAvgExt3 extends GAPNodeAvg {

	public GAPNodeAvgExt3(String prefix) {
		super(prefix);
	}

	@Override
	public void setInit(double nodeId, double err) {
		this.myId = nodeId;
		if (nodeId == 0) {
			this.level = 0;
			this.parent = 0;
		} else {
			this.parent = Double.POSITIVE_INFINITY;
			this.level = Double.POSITIVE_INFINITY;
		}
		this.totalReqNumInSubtree = 0;
		this.totalReqNumLocal = 0;
		this.totalReqTimeInSubtree = 0;
		this.totalReqTimeLocal = 0;
		this.estimatedAverage = 0;
		this.estimatedMax = 0;
		this.virgin = true;
		if (nodeId == 0)
			this.errorBudgetInSubtree = err;
	}

}
