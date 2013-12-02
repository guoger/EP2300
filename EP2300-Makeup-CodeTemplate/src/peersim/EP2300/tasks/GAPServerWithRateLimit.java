package peersim.EP2300.tasks;

import peersim.EP2300.message.ResponseTimeArriveMessage;
import peersim.EP2300.message.TimeOut;
import peersim.EP2300.message.UpdateVectorMax;
import peersim.EP2300.transport.ConfigurableDelayTransport;
import peersim.EP2300.transport.InstantaneousTransport;
import peersim.EP2300.util.NodeUtils;
import peersim.EP2300.vector.GAPNodeMax;
import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

public class GAPServerWithRateLimit extends GAPNodeMax implements EDProtocol,
		CDProtocol {

	/**
	 * Initial message budget. Defaults to 5
	 */
	protected static final String MESSAGE_BUDGET = "rate_control";
	// private long timeWindow = 0;

	protected double msgBudget;
	public UpdateVectorMax msgToSend;

	public GAPServerWithRateLimit(String prefix) {
		super(prefix);
		msgBudget = 99999; // for warm-up phase, we don't constrain message rate
		timeWindow = Configuration.getLong("delta_t");
	}

	// If protocol and control are in the same package, this method could be
	// protected
	public void resetMsgBudget() {
		this.msgBudget = 1;
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
		// if (this.level == Double.POSITIVE_INFINITY)
		// System.err.println("Still Orphan");
		if (event instanceof ResponseTimeArriveMessage) {
			final ResponseTimeArriveMessage newRequest = (ResponseTimeArriveMessage) event;
			long resTime = newRequest.getResponseTime();
			this.requestList.add(resTime);
			scheduleATimeOut(pid, resTime);
			long oldMaxReqTimeInSubtree = this.maxReqTimeInSubtree;
			this.maxReqTimeInSubtree = computeSubtreeValue();
			if (this.maxReqTimeInSubtree != oldMaxReqTimeInSubtree) {
				this.estimatedMax = this.maxReqTimeInSubtree;
				// vector != newvector
				sendMsgToParent(node, pid);
			}
		} else if (event instanceof UpdateVectorMax) {
			final UpdateVectorMax msg = (UpdateVectorMax) event;
			// store old data
			long oldMaxReqTimeInSubtree = this.maxReqTimeInSubtree;
			double oldLevel = this.level;
			double oldParent = this.parent;
			updateEntry(msg);
			findNewParent();
			this.maxReqTimeInSubtree = computeSubtreeValue();

			if (this.level != oldLevel || this.parent != oldParent) {
				this.estimatedMax = this.maxReqTimeInSubtree;
				sendMsgToAllNeighbor(node, pid);
				return;
			}
			if (this.maxReqTimeInSubtree != oldMaxReqTimeInSubtree) {
				this.estimatedMax = this.maxReqTimeInSubtree;
				// vector != newvector
				sendMsgToParent(node, pid);
			}

		} else if (event instanceof TimeOut) {
			// receive a time out message, reset aggregate value of
			// corresponding entry
			/*
			 * REPORT a tradeoff between overhead and accuracy will occur here,
			 * depending on the approach to realize node expiration
			 */
			final TimeOut msg = (TimeOut) event;
			this.requestList.remove(msg.element);
			long oldMaxReqTimeInSubtree = this.maxReqTimeInSubtree;
			computeLocalValue();
			this.maxReqTimeInSubtree = computeSubtreeValue();
			if (this.maxReqTimeInSubtree != oldMaxReqTimeInSubtree) {
				this.estimatedMax = this.maxReqTimeInSubtree;
				// vector != newvector
				sendMsgToParent(node, pid);
			}
		}

	}

	private void scheduleATimeOut(int pid, long element) {
		TimeOut timeOut = new TimeOut(element);
		Node dest = NodeUtils.getInstance().getNodeByID((long) this.myId);
		ConfigurableDelayTransport transport = ConfigurableDelayTransport
				.getInstance();
		transport.setDelay(this.timeWindow);
		transport.send(null, dest, timeOut, pid);
	}

	private void sendMsgToParent(Node node, int pid) {
		if (this.parent == Double.POSITIVE_INFINITY)
			return;
		if (this.virgin == true && this.myId == 0) {
			sendMsgToAllNeighbor(node, pid);
			this.virgin = false;
		} else {
			Linkable linkable = (Linkable) node.getProtocol(FastConfig
					.getLinkable(pid));
			UpdateVectorMax newMessage = composeMessage(node);
			for (int i = 0; i < linkable.degree(); ++i) {
				if (msgBudget < 1)
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
		UpdateVectorMax newMessage = composeMessage(node);
		if (linkable.degree() > 0) {
			for (int i = 0; i < linkable.degree(); ++i) {
				if (msgBudget < 1)
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
