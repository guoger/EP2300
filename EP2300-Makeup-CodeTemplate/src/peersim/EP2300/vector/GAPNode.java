package peersim.EP2300.vector;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import peersim.EP2300.base.GAPProtocolBase;
import peersim.EP2300.message.UpdateVector;
import peersim.EP2300.util.NodeStateVector;
import peersim.core.Node;
import peersim.core.Protocol;

public class GAPNode extends GAPProtocolBase implements Protocol {
	protected double parent;
	public double me;
	protected double level;
	public long value; // local value (response time)
	protected long aggregate; // sum of subtree (including self)
	protected int resetLock; // REPORT a reset lock to prevent an entry being
								// reseted after a fresher update
	protected long activeNodes; // number of active nodes in whole subtree,
								// including self

	// ********************************************

	public SortedMap<Double, NodeStateVector> neighborList;

	// *********************************************
	// ***set initial values, set by initializer****
	public void setInit(double nodeId) {
		this.me = nodeId;
		if (nodeId == 0) {
			// I'm root
			this.level = 0;
			this.parent = 0;
		} else {
			this.parent = Double.POSITIVE_INFINITY;
			this.level = Double.POSITIVE_INFINITY;
		}
		this.value = -1;
		this.aggregate = 0;
		this.resetLock = 0;
		this.activeNodes = 0;
		this.estimatedAverage = 0;
		this.estimatedMax = 0;
	}

	// TODO it will bring some benefits if we divide table into three separate
	// map: children, parent, peers
	// Although, maintaining spanning tree will cost more. Tradeoff!!
	// TODO how to deal with link fail rather than node fail? Should extend DIM?
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
		long aggregate = msg.aggregate;
		double senderLevel = msg.level;
		long activeNodes = msg.activeNodes;
		NodeStateVector nodeStateVector = null;
		if (srcParent == this.me) {
			// message from child (both new and old)
			nodeStateVector = new NodeStateVector("child", senderLevel,
					aggregate, activeNodes);
		} else if (srcId == this.parent) {
			// message from parent
			nodeStateVector = new NodeStateVector("parent", senderLevel,
					aggregate, activeNodes);
		} else {
			// message from peer
			nodeStateVector = new NodeStateVector("peer", senderLevel,
					aggregate, activeNodes);
		}
		this.neighborList.put(srcId, nodeStateVector);
	}

	public boolean findNewParent() {
		if (this.me == 0)
			// I'm root, I don't have parent
			return false;
		if (this.neighborList.isEmpty())
			return false;
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
				Iterator iterator = this.neighborList.keySet().iterator();
				while (iterator.hasNext()) {
					Object key = iterator.next();
					// System.out.println("key : " + key + " value :"
					// + this.neighborList.get(key).status + " | "
					// + this.neighborList.get(key).level + " | "
					// + this.neighborList.get(key).aggregate);
				}
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
	 * Refresh aggregate value by sum up aggregate value of all children and
	 * self local value
	 */
	public long computeAggregate() {
		long agg = 0;
		long active = 0;
		if (neighborList.isEmpty()) {
			this.aggregate = this.value;
			return this.aggregate;
		}
		for (Entry<Double, NodeStateVector> entry : neighborList.entrySet()) {
			double id = entry.getKey();
			NodeStateVector nodeStateVector = entry.getValue();
			if (nodeStateVector.status.equals("child")) {
				agg += nodeStateVector.aggregate;
				active += nodeStateVector.activeNodeNumber;
			}
		}
		if (this.value != -1) {
			// if this node is active
			agg += this.value;
			active++;
		}
		// System.out.println("New agg value" + this.value);
		this.aggregate = agg;
		this.activeNodes = active;
		if (this.me == 0 && this.activeNodes != 0) {
			this.estimatedAverage = this.aggregate / this.activeNodes;
		}
		return agg;
	}

	public UpdateVector composeMessage(Node node) {
		return new UpdateVector(node, this.level, this.parent, this.aggregate,
				this.activeNodes);
	}

	public void removeEntry(double id) {
		this.neighborList.remove(id);
	}

	public GAPNode(String prefix) {
		super(prefix);
		this.neighborList = new TreeMap<Double, NodeStateVector>();
	}
}
