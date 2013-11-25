package peersim.EP2300.tasks;

import peersim.EP2300.message.ResponseTimeArriveMessage;
import peersim.EP2300.message.UpdateVector;
import peersim.EP2300.transport.InstantaneousTransport;
import peersim.EP2300.vector.GAPNode;
import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

public class GAPServerWithRateLimit extends GAPNode implements EDProtocol,
		CDProtocol {

	/**
	 * Initial message budget. Defaults to 5
	 */
	protected static final String MESSAGE_BUDGET = "rate_control";

	private final int msgBudget_value;

	protected int msgBudget;
	public UpdateVector msgToSend;

	public GAPServerWithRateLimit(String prefix) {
		super(prefix);
		msgBudget_value = (Configuration.getInt(prefix + "." + MESSAGE_BUDGET,
				5));
		msgBudget = msgBudget_value;
	}

	// If protocol and control are in the same package, this method could be
	// protected
	public void resetMsgBudget() {
		this.msgBudget = msgBudget_value;
	}

	protected void sendWithInstTransport(Node src, Node dest, Object event) {
		int pid = Configuration.getPid("ACTIVE_PROTOCOL");

		InstantaneousTransport t = (InstantaneousTransport) src
				.getProtocol(FastConfig.getTransport(pid));
		t.send(src, dest, event, pid);
	}

	@Override
	public void processEvent(Node node, int pid, Object event) {
		// Implement your event-driven code for task 1 here
		/*
		 * in task 1, message comes from: 1) control: ResponseTimeArriveMessage,
		 * indicating the change of aggregate 2) node: update state from
		 * neightbor/child/parent
		 */

		if (event instanceof ResponseTimeArriveMessage) {
			// TODO message from internal load generator, update local value,
			// compute new aggregate value
			// and send out updated vector to all neighbors (potentially could
			// only send it to parent to reduce overhead)
			final ResponseTimeArriveMessage loadChangeMsg = (ResponseTimeArriveMessage) event;
			this.value = loadChangeMsg.getResponseTime();
			long oldAgg = this.aggregate;
			computeAggregate();
			if (this.aggregate != oldAgg) {
				sendMsg(node, pid);
			}
		} else if (event instanceof UpdateVector) {
			final UpdateVector msg = (UpdateVector) event;
			updateEntry(msg);
			boolean findNewParent = findNewParent();
			long oldAgg = this.aggregate;
			computeAggregate();
			if (findNewParent || this.aggregate != oldAgg) {
				// vector != newvector
				sendMsg(node, pid);
			}
		}

	}

	/**
	 * Send out message to all neighbors
	 * 
	 * @param node
	 * @param pid
	 */
	private void sendMsg(Node node, int pid) {
		Linkable linkable = (Linkable) node.getProtocol(FastConfig
				.getLinkable(pid));
		UpdateVector newMessage = composeMessage(node);
		if (linkable.degree() > 0) {
			for (int i = 0; i < linkable.degree(); ++i) {
				if (msgBudget <= 0)
					return; // no message budget left, simply return
				Node peer = linkable.getNeighbor(i);
				// The selected peer could be inactive
				if (!peer.isUp())
					continue;
				InstantaneousTransport transport = new InstantaneousTransport();
				transport.send(node, peer, newMessage, pid);
				msgBudget--;
			}
		}
	}

	@Override
	public void nextCycle(Node node, int protocolID) {
		// Implement your cycle-driven code for task 1 here
	}

	public GAPServerWithRateLimit clone() {
		return new GAPServerWithRateLimit(prefix);
	}

}
