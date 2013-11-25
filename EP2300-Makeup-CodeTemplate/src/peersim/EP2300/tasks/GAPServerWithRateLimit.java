package peersim.EP2300.tasks;

import peersim.EP2300.message.ResponseTimeArriveMessage;
import peersim.EP2300.message.UpdateVector;
import peersim.EP2300.transport.InstantaneousTransport;
import peersim.EP2300.vector.GAPNode;
import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

public class GAPServerWithRateLimit extends GAPNode implements EDProtocol,
		CDProtocol {

	public int msgBudget;
	public UpdateVector msgToSend;

	public GAPServerWithRateLimit(String prefix) {
		super(prefix);
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
			// TODO message from internal load generator, update aggregate value
			// and send out updated vector to all neighbors (potentially could
			// only send it to parent to reduce overhead)
			final ResponseTimeArriveMessage loadChangeMsg = (ResponseTimeArriveMessage) event;
			this.aggregate = loadChangeMsg.getResponseTime();
		} else if (event instanceof UpdateVector) {
			// TODO message from other nodes, update table accordingly and send
			// out message
			final UpdateVector msg = (UpdateVector) event;
			updateEntry(msg);
			boolean findShorterPath = findNewParent();
		}

	}

	@Override
	public void nextCycle(Node node, int protocolID) {
		// Implement your cycle-driven code for task 1 here
		/*
		 * TODO nextCycle is implemented here to reset message budget
		 */
	}

	public GAPServerWithRateLimit clone() {
		return new GAPServerWithRateLimit(prefix);
	}

}
