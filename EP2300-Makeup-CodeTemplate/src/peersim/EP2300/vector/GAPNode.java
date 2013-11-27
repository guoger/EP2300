package peersim.EP2300.vector;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import peersim.EP2300.base.GAPProtocolBase;
import peersim.EP2300.message.UpdateVector;
import peersim.EP2300.util.NodeStateVector;
import peersim.config.Configuration;
import peersim.core.Node;
import peersim.core.Protocol;

public class GAPNode extends GAPProtocolBase implements Protocol {
	/*
	 * These variables are public because they need to be accessible by observer
	 */
	public double parent;
	public double myId;
	public double level;
	public long totalReqTimeInSubtree;
	public long totalReqTimeLocal;
	public long totalReqNumInSubtree;
	public long totalReqNumLocal;
	public long maxReqTimeInSubtree;
	public long maxReqTimeLocal;
	protected boolean virgin;
	public long nodeNumInSubtree;

	public long timeWindow = -1;

	// ********************************************

	public SortedMap<Double, NodeStateVector> neighborList;
	public ArrayList<Long> requestList;

	// *********************************************
	// ***set initial values, set by initializer****
	public void setInit(double nodeId) {
		this.myId = nodeId;
		if (nodeId == 0) {
			System.err.println("I'm root node!");
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
		this.maxReqTimeInSubtree = 0;
		this.estimatedAverage = 0;
		this.estimatedMax = 0;
		this.virgin = true;
		this.nodeNumInSubtree = 1;
	}

	public GAPNode(String prefix) {
		super(prefix);
		this.neighborList = new TreeMap<Double, NodeStateVector>();
		this.requestList = new ArrayList<Long>();
		timeWindow = Configuration.getLong("delta_t");
	}

	/*
	 * refresh table in order to maintain spanning tree as well as computing new
	 * aggregate value Return true if an update message need to sent, otherwise
	 * return false
	 */
	public void updateEntry(UpdateVector msg) {
		if (msg.sender == null) {
			return;
			// should never happen since we don't acknowledge msg in GAP
		}
		double srcParent = msg.parent;
		double srcId = msg.sender.getID();
		double srcLevel = msg.level;
		long totalReqTime = msg.totalReqTimeInSubtree;
		long totalReqNum = msg.totalReqNumInSubtree;
		long maxReqTime = msg.maxReqTimeInSubtree;
		long nodeCount = msg.nodeNumInSubtree;
		NodeStateVector nodeStateVector = null;
		if (srcParent == this.myId) {
			// message from child (both new and old)
			nodeStateVector = new NodeStateVector("child", srcLevel,
					totalReqTime, totalReqNum, maxReqTime, nodeCount);
		} else if (srcId == this.parent) {
			// message from parent
			nodeStateVector = new NodeStateVector("parent", srcLevel,
					totalReqTime, totalReqNum, maxReqTime, nodeCount);
		} else {
			// message from peer
			nodeStateVector = new NodeStateVector("peer", srcLevel,
					totalReqTime, totalReqNum, maxReqTime, nodeCount);
		}
		this.neighborList.put(srcId, nodeStateVector);
	}

	public boolean findNewParent() {
		if (this.myId == 0)
			// I'm root, I don't have parent
			return false;
		if (this.neighborList.isEmpty())
			return false;
		boolean findParent = false;
		double minLevel = Double.POSITIVE_INFINITY;
		double newParent = this.parent;
		for (Entry<Double, NodeStateVector> entry : neighborList.entrySet()) {
			double id = entry.getKey();
			NodeStateVector nodeStateVector = entry.getValue();
			// find a node with smallest level

			if (nodeStateVector.level < minLevel) {
				// TODO is it necessary to choose the smallest ID? It's realized
				// by using sorted map
				minLevel = nodeStateVector.level;
				newParent = id;
			}
		}

		if (newParent != this.parent) {
			if (this.parent != Double.POSITIVE_INFINITY) {
				/*
				 * Found a shorter path to root, and current parent exist:
				 * replace parent with new one
				 */
				this.neighborList.get(this.parent).status = "peer";
				this.parent = newParent;
				this.level = minLevel + 1;
				this.neighborList.get(newParent).status = "parent";
				return true;
			} else {
				/*
				 * There's no valid parent yet
				 */
				this.parent = newParent;
				this.level = minLevel + 1;
				this.neighborList.get(this.parent).status = "parent";
				return true;
			}
		} else {
			// otherwise, remain the same
			return false;
		}
	}

	/**
	 * Update table according to local value and children value
	 */
	public void computeSubtreeValue() {
		long totalReqTime = 0;
		long activeReqNum = 0;
		long maxReqTime = 0;
		long nodeCount = 1;
		for (Entry<Double, NodeStateVector> entry : neighborList.entrySet()) {
			double id = entry.getKey();
			NodeStateVector nodeStateVector = entry.getValue();
			if (nodeStateVector.status.equals("child")) {
				totalReqTime += nodeStateVector.totalReqTime;
				activeReqNum += nodeStateVector.totalReqNum;
				nodeCount += nodeStateVector.nodeNum;
				if (nodeStateVector.maxReqTime > maxReqTime) {
					maxReqTime = nodeStateVector.maxReqTime;
				}
			}
		}
		// System.out.println("New agg value" + this.value);
		totalReqNumInSubtree = totalReqNumLocal + activeReqNum;
		totalReqTimeInSubtree = totalReqTimeLocal + totalReqTime;
		nodeNumInSubtree = nodeCount;
		if (level == 5) {
			// printNeighbor();
			// printMyState();
		}
		maxReqTimeInSubtree = (maxReqTimeLocal > maxReqTime) ? this.maxReqTimeLocal
				: maxReqTime;

		estimatedMax = maxReqTimeInSubtree;
		if (totalReqNumInSubtree != 0) {
			estimatedAverage = (double) totalReqTimeInSubtree
					/ (double) totalReqNumInSubtree;
		} else {
			estimatedAverage = 0;
		}
	}

	/**
	 * Update local value
	 * 
	 * @return
	 */
	public void computeLocalValue() {
		long sum = 0;
		long max = 0;
		for (int i = 0; i < requestList.size(); i++) {
			sum += requestList.get(i);
			if (requestList.get(i) > max)
				max = requestList.get(i);
		}
		this.totalReqNumLocal = requestList.size();
		this.totalReqTimeLocal = sum;
		this.maxReqTimeLocal = max;
	}

	public UpdateVector composeMessage(Node node) {
		UpdateVector outMsg = new UpdateVector(node, level, parent,
				totalReqTimeInSubtree, totalReqNumInSubtree,
				maxReqTimeInSubtree, nodeNumInSubtree);
		return outMsg;
	}

	// **********************************************************************
	// ################## for debug #########################################

	public void printNeighbor() {
		System.err.println(myId);
		for (Entry<Double, NodeStateVector> entry : neighborList.entrySet()) {
			double id = entry.getKey();
			NodeStateVector nodeStateVector = entry.getValue();
			System.err.println("\t" + id + "\t==>\t" + nodeStateVector.level
					+ "\t" + nodeStateVector.status + "\t"
					+ nodeStateVector.nodeNum);
		}
	}

	public void printMyState() {
		System.err.println(myId + ":\n\t" + "nodeNumInSubtree: "
				+ nodeNumInSubtree);
	}
}
