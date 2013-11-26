package peersim.EP2300.tasks;

import peersim.EP2300.message.ResponseTimeArriveMessage;
import peersim.EP2300.message.TimeOut;
import peersim.EP2300.message.UpdateVector;
import peersim.EP2300.transport.ConfigurableDelayTransport;
import peersim.EP2300.transport.InstantaneousTransport;
import peersim.EP2300.util.NodeUtils;
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

	private long timeWindow = 0;

	protected int msgBudget;
	public UpdateVector msgToSend;

	public GAPServerWithRateLimit(String prefix) {
		super(prefix);
		msgBudget_value = (Configuration.getInt(prefix + "." + MESSAGE_BUDGET,
				5));
		msgBudget = msgBudget_value;
		timeWindow = Configuration.getLong("delta_t");
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
			final ResponseTimeArriveMessage loadChangeMsg = (ResponseTimeArriveMessage) event;
			this.value = loadChangeMsg.getResponseTime();
			scheduleATimeOut(pid);
			this.resetLock++;
			// System.out.print("Load change: " + this.value);
			long oldAgg = this.aggregate;
			computeAggregate();
			if (this.aggregate != oldAgg) {
				sendMsgToParent(node, pid);
			}
		} else if (event instanceof UpdateVector) {
			final UpdateVector msg = (UpdateVector) event;
			// System.out.println("Receive msg from neighbor");
			updateEntry(msg);
			double oldLevel = this.level;
			boolean findNewParent = findNewParent();
			long oldAgg = this.aggregate;
			computeAggregate();
			if (this.aggregate != oldAgg) {
				// vector != newvector
				if (this.level != oldLevel) {
					// if level changed, send to all neighbors
					sendMsgToAllNeighbor(node, pid);
				} else {
					// otherwise, send to parent only
					sendMsgToParent(node, pid);
				}
			}
		} else if (event instanceof TimeOut) {
			// receive a time out message, reset aggregate value of
			// corresponding entry
			/*
			 * REPORT a tradeoff between overhead and accuracy will occur here,
			 * depending on the approach to realize node expiration
			 */
			this.resetLock--;
			if (this.resetLock == 0) {
				this.value = 0;
				long oldAgg = this.aggregate;
				computeAggregate();
				if (this.aggregate != oldAgg) {
					sendMsgToParent(node, pid);
				}
			}

		}

	}

	private void scheduleATimeOut(int pid) {
		TimeOut timeOut = new TimeOut();
		Node dest = NodeUtils.getInstance().getNodeByID((long) this.me);
		ConfigurableDelayTransport transport = ConfigurableDelayTransport
				.getInstance();
		transport.setDelay(timeWindow);
		transport.send(null, dest, timeOut, pid);

	}

	private void sendMsgToParent(Node node, int pid) {
		// TODO send msg to parent. If this is root node, send to all neighbors,
		// as a heart beat (actually, it's for initialization, DIRTY approach!
		if (this.me == 0 || this.parent == Double.POSITIVE_INFINITY) {
			sendMsgToAllNeighbor(node, pid);
		} else {
			Linkable linkable = (Linkable) node.getProtocol(FastConfig
					.getLinkable(pid));
			UpdateVector newMessage = composeMessage(node);
			for (int i = 0; i < linkable.degree(); ++i) {
				if (msgBudget <= 0)
					return; // no message budget left, simply return
				Node peer = linkable.getNeighbor(i);
				if (peer.getID() == this.parent && peer.isUp()) {
					InstantaneousTransport transport = new InstantaneousTransport();
					transport.send(node, peer, newMessage, pid);
					msgBudget--;
					return;
				}
			}
		}
	}

	/**
	 * Send out message to all neighbors
	 * 
	 * @param node
	 * @param pid
	 */
	private void sendMsgToAllNeighbor(Node node, int pid) {
		// System.out.println("Have msg budget" + this.msgBudget);
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
