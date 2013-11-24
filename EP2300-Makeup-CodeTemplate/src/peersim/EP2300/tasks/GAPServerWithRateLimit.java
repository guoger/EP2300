package peersim.EP2300.tasks;

import java.util.Map.Entry;
import java.util.SortedMap;

import peersim.EP2300.base.GAPProtocolBase;
import peersim.EP2300.message.UpdateVector;
import peersim.EP2300.util.NodeStateVector;
import peersim.cdsim.CDProtocol;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

public class GAPServerWithRateLimit extends GAPProtocolBase implements
		EDProtocol, CDProtocol {

	// TODO need to be initialized by init control
	// ********************************************
	public int parent;
	public int me;
	public double level;
	public float aggregate;
	// ********************************************

	public int msgBudget;

	public SortedMap<Integer, NodeStateVector> neighborList;

	public UpdateVector msgToSend;

	public GAPServerWithRateLimit(String prefix) {
		super(prefix);
	}

	// TODO it will bring some benefits if we divide table into three separate
	// map: children, parent, peers
	// Although, maintaining spanning tree will cost more. Tradeoff!!
	// TODO how to deal with link fail rather than node fail? Should extend DIM?
	public void updateEntry(int id, double parent, double level, float aggregate) {
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

	/*
	 * refresh table in order to maintain spanning tree as well as computing new
	 * aggregate value Return true if an update message need to sent, otherwise
	 * return false
	 */
	public boolean refreshState() {
		// Maintain spanning tree and calculate new local aggregate
		double minLevel = Double.POSITIVE_INFINITY;
		int minID = parent;
		float aggregate = 0;
		for (Entry<Integer, NodeStateVector> entry : neighborList.entrySet()) {
			int id = entry.getKey();
			NodeStateVector nodeStateVecotr = entry.getValue();
			// compute subtree aggregate value
			if (nodeStateVecotr.status.equals("child")) {
				aggregate += nodeStateVecotr.aggregate;
			}
			// find a node with smallest level
			if (nodeStateVecotr.level < minLevel) {
				// TODO is it necessary to choose the smallest ID? It's realized
				// by using sorted map
				minLevel = nodeStateVecotr.level;
				minID = id;
			}
		}
		// If parent ref is changed
		if (minID != parent) {
			// A shorter path to root found
			this.level = minLevel + 1;
			this.parent = minID;
			this.neighborList.get(minID).status = "parent";
			this.msgToSend.set(me, level, parent, aggregate);
			return true;
		}
		// if aggregate value is changed
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
		this.neighborList.remove(id);
	}

	@Override
	public void processEvent(Node node, int pid, Object event) {
		// Implement your event-driven code for task 1 here
		/*
		 * in task 1, message occurs when local value (response time) is changed
		 * using ResponseTimeArriveMessage class, along with stateVector
		 */

	}

	@Override
	public void nextCycle(Node node, int protocolID) {
		// Implement your cycle-driven code for task 1 here
		/*
		 * nextCycle is implemented here to reset message budget
		 */
	}

	public GAPServerWithRateLimit clone() {
		return new GAPServerWithRateLimit(prefix);
	}

}
