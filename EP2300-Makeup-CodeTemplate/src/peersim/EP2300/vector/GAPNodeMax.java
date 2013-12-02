package peersim.EP2300.vector;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import peersim.EP2300.base.GAPProtocolBase;
import peersim.EP2300.message.UpdateVectorMax;
import peersim.EP2300.util.NodeStateVectorMax;
import peersim.config.Configuration;
import peersim.core.Node;
import peersim.core.Protocol;

public class GAPNodeMax extends GAPProtocolBase implements Protocol {
	/*
	 * These variables are public because they need to be accessible by observer
	 */
	public double parent;
	public double myId;
	public double level;
	public long maxReqTimeInSubtree;
	public long maxReqTimeLocal;
	protected boolean virgin;
	public long nodeNumInSubtree;
	public double errorBudget = 0;
	public long timeWindow = -1;

	// ********************************************

	public SortedMap<Double, NodeStateVectorMax> neighborList;
	public ArrayList<Long> requestList;

	// *********************************************
	// ***set initial values, set by initializer****
	public void setInit(double nodeId, double err) {
		this.myId = nodeId;
		if (nodeId == 0) {
			System.err.println("I'm root node!");
			this.level = 0;
			this.parent = 0;
		} else {
			this.parent = Double.POSITIVE_INFINITY;
			this.level = Double.POSITIVE_INFINITY;
		}
		this.maxReqTimeInSubtree = 0;
		this.estimatedAverage = 0;
		this.estimatedMax = 0;
		this.virgin = true;
		this.errorBudget = err;
		System.out.println("Error Budget is: " + this.errorBudget);
	}

	public GAPNodeMax(String prefix) {
		super(prefix);
		this.neighborList = new TreeMap<Double, NodeStateVectorMax>();
		this.requestList = new ArrayList<Long>();
		timeWindow = Configuration.getLong("delta_t");
	}

	/**
	 * refresh table in order to maintain spanning tree as well as computing new
	 * aggregate value Return true if an update message need to sent, otherwise
	 * return false
	 */
	public void updateEntry(UpdateVectorMax msg) {
		if (msg.sender == null) {
			return;
			// should never happen since we don't acknowledge msg in GAP
		}
		double srcParent = msg.parent;
		double srcId = msg.sender.getID();
		double srcLevel = msg.level;
		long maxReqTime = msg.maxReqTimeInSubtree;
		NodeStateVectorMax nodeStateVector = null;
		if (srcParent == this.myId) {
			// message from child (both new and old)
			nodeStateVector = new NodeStateVectorMax("child", srcLevel,
					maxReqTime);
		} else if (srcId == this.parent) {
			// message from parent
			nodeStateVector = new NodeStateVectorMax("parent", srcLevel,
					maxReqTime);
		} else {
			// message from peer
			nodeStateVector = new NodeStateVectorMax("peer", srcLevel,
					maxReqTime);
		}
		this.neighborList.put(srcId, nodeStateVector);
	}

	/**
	 * Traverse subtree table and search for a new parent with shorter path to
	 * root. If new parent found, return true, otherwise, return false
	 * 
	 * @return
	 */
	public boolean findNewParent() {
		if (this.myId == 0)
			// I'm root, I don't have parent
			return false;
		if (this.neighborList.isEmpty())
			return false;
		double minLevel = Double.POSITIVE_INFINITY;
		double newParent = this.parent;
		for (Entry<Double, NodeStateVectorMax> entry : neighborList.entrySet()) {
			double id = entry.getKey();
			NodeStateVectorMax nodeStateVector = entry.getValue();
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
	public long computeSubtreeValue() {
		long localMax = computeLocalValue();
		long subtreeMax = 0;
		for (Entry<Double, NodeStateVectorMax> entry : neighborList.entrySet()) {
			NodeStateVectorMax nodeStateVector = entry.getValue();
			if (nodeStateVector.status.equals("child")) {
				if (nodeStateVector.maxReqTime > subtreeMax) {
					subtreeMax = nodeStateVector.maxReqTime;
				}
			}
		}
		if (localMax > subtreeMax)
			subtreeMax = localMax;
		return subtreeMax;
	}

	/**
	 * Update local value
	 * 
	 * @return
	 */
	public long computeLocalValue() {
		long max = 0;
		for (int i = 0; i < requestList.size(); i++) {
			long value = requestList.get(i);
			if (value > max)
				max = value;
		}
		return max;
	}

	public UpdateVectorMax composeMessage(Node node) {
		UpdateVectorMax outMsg = new UpdateVectorMax(node, level, parent,
				maxReqTimeInSubtree);
		return outMsg;
	}

	// **********************************************************************
	// ################## for debug #########################################

	public void printNeighbor() {
		System.err.println(myId);
		for (Entry<Double, NodeStateVectorMax> entry : neighborList.entrySet()) {
			double id = entry.getKey();
			NodeStateVectorMax nodeStateVector = entry.getValue();
			System.err.println("\t" + id + "\t==>\t" + nodeStateVector.level
					+ "\t" + nodeStateVector.status);
		}
	}

	public void printMyState() {
		System.err.println(myId + ":\n\t" + "nodeNumInSubtree: "
				+ nodeNumInSubtree);
	}
}
