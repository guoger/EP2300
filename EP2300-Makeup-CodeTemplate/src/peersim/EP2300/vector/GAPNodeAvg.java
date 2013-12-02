package peersim.EP2300.vector;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import peersim.EP2300.base.GAPProtocolBase;
import peersim.EP2300.message.UpdateVectorAvg;
import peersim.EP2300.util.NodeStateVectorTask2;
import peersim.config.Configuration;
import peersim.core.Node;
import peersim.core.Protocol;

public class GAPNodeAvg extends GAPProtocolBase implements Protocol {

	public double parent;
	public double myId;
	public double level;
	public long totalReqTimeInSubtree;
	public long totalReqTimeLocal;
	public long totalReqNumInSubtree;
	public long totalReqNumLocal;
	protected boolean virgin;
	public double errorBudget = 0;
	public long timeWindow = -1;

	public SortedMap<Double, NodeStateVectorTask2> neighborList;
	public ArrayList<Long> requestList;

	public GAPNodeAvg(String prefix) {
		super(prefix);
		this.neighborList = new TreeMap<Double, NodeStateVectorTask2>();
		this.requestList = new ArrayList<Long>();
		timeWindow = Configuration.getLong("delta_t");
	}

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
		this.errorBudget = err;
	}

	/**
	 * refresh table in order to maintain spanning tree as well as computing new
	 * aggregate value Return true if an update message need to sent, otherwise
	 * return false
	 */
	public void updateEntry(UpdateVectorAvg msg) {
		if (msg.sender == null) {
			return;
			// should never happen since we don't acknowledge msg in GAP
		}
		double srcParent = msg.parent;
		double srcId = msg.sender.getID();
		double srcLevel = msg.level;
		long totalReqTime = msg.totalReqTimeInSubtree;
		long totalReqNum = msg.totalReqNumInSubtree;
		NodeStateVectorTask2 nodeStateVector = null;
		if (srcParent == this.myId) {
			// message from child (both new and old)
			nodeStateVector = new NodeStateVectorTask2("child", srcLevel,
					totalReqTime, totalReqNum);
		} else if (srcId == this.parent) {
			// message from parent
			nodeStateVector = new NodeStateVectorTask2("parent", srcLevel,
					totalReqTime, totalReqNum);
		} else {
			// message from peer
			nodeStateVector = new NodeStateVectorTask2("peer", srcLevel,
					totalReqTime, totalReqNum);
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
		for (Entry<Double, NodeStateVectorTask2> entry : neighborList
				.entrySet()) {
			double id = entry.getKey();
			NodeStateVectorTask2 nodeStateVector = entry.getValue();
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

	public void computeSubtreeValue() {
		long totalReqTime = 0;
		long activeReqNum = 0;
		long maxReqTime = 0;
		for (Entry<Double, NodeStateVectorTask2> entry : neighborList
				.entrySet()) {
			double id = entry.getKey();
			NodeStateVectorTask2 nodeStateVector = entry.getValue();
			if (nodeStateVector.status.equals("child")) {
				totalReqTime += nodeStateVector.totalReqTime;
				activeReqNum += nodeStateVector.totalReqNum;
			}
		}
		// System.out.println("New agg value" + this.value);
		totalReqNumInSubtree = totalReqNumLocal + activeReqNum;
		totalReqTimeInSubtree = totalReqTimeLocal + totalReqTime;
		if (totalReqNumInSubtree != 0) {
			estimatedAverage = (double) totalReqTimeInSubtree
					/ (double) totalReqNumInSubtree;
		} else {
			estimatedAverage = 0;
		}
	}

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
	}

	public UpdateVectorAvg composeMessage(Node node) {
		UpdateVectorAvg outMsg = new UpdateVectorAvg(node, level, parent,
				totalReqTimeInSubtree, totalReqNumInSubtree);
		return outMsg;
	}
}
