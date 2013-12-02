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

public class GAPExtension1 extends GAPNodeMax implements EDProtocol, CDProtocol {

	/**
	 * Initial message budget. Defaults to 5
	 */
	private long lastReportedMax;

	public GAPExtension1(String prefix) {
		super(prefix);
		lastReportedMax = 0;
		timeWindow = Configuration.getLong("delta_t");
	}

	protected void sendWithInstTransport(Node src, Node dest, Object event) {
		int pid = Configuration.getPid("ACTIVE_PROTOCOL");

		InstantaneousTransport t = (InstantaneousTransport) src
				.getProtocol(FastConfig.getTransport(pid));
		t.send(src, dest, event, pid);
	}

	/**
	 * Test whether the change exceed error limit
	 * 
	 * @return
	 */
	private boolean testDiff(long newMax) {
		// System.out.println("error budget is: " + errorBudget);
		if (Math.abs(newMax - lastReportedMax) > errorBudget) {
			lastReportedMax = newMax;
			return true;
		} else {
			return false;
		}
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
			long newMax = computeSubtreeValue();
			// System.out.println("Error budget is:" + errorBudget);
			if (testDiff(newMax)) {
				estimatedMax = newMax;
				sendMsgToParent(node, pid, newMax);
			}
		} else if (event instanceof UpdateVectorMax) {
			final UpdateVectorMax msg = (UpdateVectorMax) event;
			// store old data
			double oldLevel = this.level;
			double oldParent = this.parent;
			updateEntry(msg);
			findNewParent();
			long newMax = computeSubtreeValue();
			if (this.level != oldLevel || this.parent != oldParent) {
				sendMsgToAllNeighbor(node, pid, newMax);
				return;
			}
			if (testDiff(newMax)) {
				estimatedMax = newMax;
				sendMsgToParent(node, pid, newMax);
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
			long newMax = computeSubtreeValue();
			if (testDiff(newMax)) {
				estimatedMax = newMax;
				sendMsgToParent(node, pid, newMax);
			}
		}

	}

	public UpdateVectorMax composeMessage(Node node, long newMax) {
		UpdateVectorMax outMsg = new UpdateVectorMax(node, level, parent,
				newMax);
		return outMsg;
	}

	private void scheduleATimeOut(int pid, long element) {
		TimeOut timeOut = new TimeOut(element);
		Node dest = NodeUtils.getInstance().getNodeByID((long) this.myId);
		ConfigurableDelayTransport transport = ConfigurableDelayTransport
				.getInstance();
		transport.setDelay(this.timeWindow);
		transport.send(null, dest, timeOut, pid);
	}

	private void sendMsgToParent(Node node, int pid, long newMax) {
		if (this.parent == Double.POSITIVE_INFINITY)
			return;
		if (this.myId == 0) {
			if (virgin) {
				sendMsgToAllNeighbor(node, pid, newMax);
				this.virgin = false;
				return;
			} else
				return;
		} else {
			Linkable linkable = (Linkable) node.getProtocol(FastConfig
					.getLinkable(pid));
			UpdateVectorMax newMessage = composeMessage(node, newMax);
			for (int i = 0; i < linkable.degree(); ++i) {
				Node peer = linkable.getNeighbor(i);
				if (peer.getID() == this.parent && peer.isUp()) {
					InstantaneousTransport transport = new InstantaneousTransport();
					transport.send(node, peer, newMessage, pid);
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
	private void sendMsgToAllNeighbor(Node node, int pid, long newMax) {
		// System.out.println("Have msg budget" + this.msgBudget);
		Linkable linkable = (Linkable) node.getProtocol(FastConfig
				.getLinkable(pid));
		UpdateVectorMax newMessage = composeMessage(node, newMax);
		if (linkable.degree() > 0) {
			for (int i = 0; i < linkable.degree(); ++i) {
				Node peer = linkable.getNeighbor(i);
				// The selected peer could be inactive
				if (!peer.isUp())
					continue;
				InstantaneousTransport transport = new InstantaneousTransport();
				transport.send(node, peer, newMessage, pid);
			}
		}
	}

	@Override
	public void nextCycle(Node node, int protocolID) {
		// Implement your cycle-driven code for task 1 here
	}

	public GAPExtension1 clone() {
		return new GAPExtension1(prefix);
	}

}
