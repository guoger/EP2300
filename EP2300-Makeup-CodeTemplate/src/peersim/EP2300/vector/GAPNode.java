package peersim.EP2300.vector;

import java.util.Map.Entry;
import java.util.SortedMap;

import peersim.EP2300.base.GAPProtocolBase;
import peersim.EP2300.message.UpdateVector;
import peersim.EP2300.util.NodeStateVector;
import peersim.core.Node;
import peersim.core.Protocol;

public class GAPNode extends GAPProtocolBase implements Protocol {

	// TODO need to be initialized by init control
	// ********************************************
	protected double parent;
	public double me;
	protected double level;
	public long value; // local value (response time)
	protected long aggregate; // sum of subtree (including self)

	// ********************************************

	public SortedMap<Double, NodeStateVector> neighborList;

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
		NodeStateVector nodeStateVector = null;
		if (srcParent == this.me) {
			// message from child (both new and old)
			nodeStateVector = new NodeStateVector("child", senderLevel,
					aggregate);
		} else if (srcId == this.parent) {
			// message from parent
			nodeStateVector = new NodeStateVector("parent", senderLevel,
					aggregate);
		} else {
			// message from peer
			nodeStateVector = new NodeStateVector("peer", senderLevel,
					aggregate);
		}
		this.neighborList.put(srcId, nodeStateVector);
	}

	public boolean findNewParent() {
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
			// found a shorter path to root, update parent
			this.neighborList.get(this.parent).status = "peer";
			this.parent = newParent;
			this.level = minLevel + 1;
			this.neighborList.get(newParent).status = "parent";
			return true;
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
		for (Entry<Double, NodeStateVector> entry : neighborList.entrySet()) {
			double id = entry.getKey();
			NodeStateVector nodeStateVector = entry.getValue();
			if (nodeStateVector.status.equals("child")) {
				agg += nodeStateVector.aggregate;
			}
		}
		agg += this.value;
		this.aggregate = agg;
		return agg;
	}

	public UpdateVector composeMessage(Node node) {
		return new UpdateVector(node, this.level, this.parent, this.aggregate);
	}

	public void removeEntry(double id) {
		this.neighborList.remove(id);
	}

	public GAPNode(String prefix) {
		super(prefix);
	}

}
