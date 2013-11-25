package peersim.EP2300.vector;

import java.util.Map.Entry;
import java.util.SortedMap;

import peersim.EP2300.base.GAPProtocolBase;
import peersim.EP2300.message.UpdateVector;
import peersim.EP2300.util.NodeStateVector;
import peersim.core.Protocol;

public class GAPNode extends GAPProtocolBase implements Protocol {

	// TODO need to be initialized by init control
	// ********************************************
	public double parent;
	public double me;
	public double level;
	public long aggregate;

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
		double id = msg.id;
		double parent = msg.parent;
		double level = msg.level;
		long aggregate = msg.aggregate;
		NodeStateVector nodeStateVector = null;
		if (parent == this.me) {
			// message from child
			nodeStateVector = new NodeStateVector("child", level, aggregate);
		} else if (id == this.parent) {
			// message from parent
			nodeStateVector = new NodeStateVector("parent", level, aggregate);
		} else {
			// message from peer
			nodeStateVector = new NodeStateVector("peer", level, aggregate);
		}
		this.neighborList.put(id, nodeStateVector);
	}

	public boolean findNewParent() {
		double minLevel = Double.POSITIVE_INFINITY;
		double newParent = this.parent;
		for (Entry<Double, NodeStateVector> entry : neighborList.entrySet()) {
			double id = entry.getKey();
			NodeStateVector nodeStateVecotr = entry.getValue();
			// find a node with smallest level
			if (nodeStateVecotr.level < minLevel) {
				// TODO is it necessary to choose the smallest ID? It's realized
				// by using sorted map
				minLevel = nodeStateVecotr.level;
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

	public void removeEntry(double id) {
		this.neighborList.remove(id);
	}

	public GAPNode(String prefix) {
		super(prefix);
		// TODO Auto-generated constructor stub
	}

}
