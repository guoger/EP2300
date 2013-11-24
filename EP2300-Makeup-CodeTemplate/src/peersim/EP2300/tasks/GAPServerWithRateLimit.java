package peersim.EP2300.tasks;

import java.util.Map;
import java.util.SortedMap;

import peersim.EP2300.base.GAPProtocolBase;
import peersim.EP2300.message.UpdateVector;
import peersim.EP2300.util.NodeID;
import peersim.EP2300.util.NodeState;
import peersim.cdsim.CDProtocol;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

public class GAPServerWithRateLimit extends GAPProtocolBase implements
		EDProtocol, CDProtocol {

	public double parent;
	public double me;
	public double level;
	public float aggregate;
	public NodeState myCurrentState;
	public SortedMap<Double, NodeState> neighborList;
	public UpdateVector msgToSend;

	public GAPServerWithRateLimit(String prefix) {
		super(prefix);
	}

	// TODO how to deal with link fail rather than node fail? Should extend DIM?
	public void updateEntry(double id, double parent, double level,
			float aggregate) {
		NodeState nodeState = null;
		if (parent == this.me) {
			// message from child
			nodeState = new NodeState("child", level, aggregate);
		} else if (id == this.parent) {
			// message from parent
			nodeState = new NodeState("parent", level, aggregate);
		} else {
			// message from peer
			nodeState = new NodeState("peer", level, aggregate);
		}
		this.neighborList.put(id, nodeState);
	}

	/*
	 * Update table in order to maintain spanning tree as well as computing new
	 * aggregate value
	 */
	public boolean updateState() {
		// Maintain spanning tree and calculate new local aggregate
		double minLevel = Double.POSITIVE_INFINITY;
		double minID = Double.POSITIVE_INFINITY;
		float aggregate = 0;
		for (Map.Entry<Double, NodeState> entry : neighborList.entrySet()) {
			double id = entry.getKey();
			NodeState nodeState = entry.getValue();
			// compute subtree aggregate value
			if (nodeState.status.equals("child")) {
				aggregate += nodeState.aggregate;
			}
			// find a node with smallest level
			if (nodeState.level < minLevel) {
				// TODO is it necessary to choose the smallest ID? It's realized
				// by using sorted map
				minLevel = nodeState.level;
				minID = id;
			}
		}
		if (minID != parent) {
			// A shorter path to root found
			this.level = minLevel + 1;
			this.parent = minID;
			this.neighborList.get(minID).status = "parent";
			this.msgToSend.set(me, level, parent, aggregate);
			return true;
		}
		// if aggregate is changed
		// TODO in Task 2 and Task 3, this part should be modified in order to
		// adapt accuracy objective
		if (this.aggregate != aggregate) {
			this.aggregate = aggregate;
			this.msgToSend.set(me, level, parent, aggregate);
			return true;
		}
		// nothing changed (not likely)
		return false;
	}

	public void removeEntry(int id) {
		NodeID nodeID = new NodeID(id);
		this.neighborList.remove(nodeID);
	}

	@Override
	public void processEvent(Node node, int pid, Object event) {
		// Implement your event-driven code for task 1 here
	}

	@Override
	public void nextCycle(Node node, int protocolID) {
		// Implement your cycle-driven code for task 1 here

	}

	public GAPServerWithRateLimit clone() {
		return new GAPServerWithRateLimit(prefix);
	}

}
