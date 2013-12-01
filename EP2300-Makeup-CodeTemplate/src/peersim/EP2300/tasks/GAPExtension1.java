package peersim.EP2300.tasks;

import peersim.EP2300.message.ErrorBudget;
import peersim.EP2300.message.ResponseTimeArriveMessage;
import peersim.EP2300.message.TimeOut;
import peersim.EP2300.message.UpdateVector;
import peersim.EP2300.transport.ConfigurableDelayTransport;
import peersim.EP2300.transport.InstantaneousTransport;
import peersim.EP2300.util.NodeStateVector;
import peersim.EP2300.util.NodeUtils;
import peersim.EP2300.vector.GAPNode;
import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

public class GAPExtension1 extends GAPNode implements EDProtocol, CDProtocol {

	private double lastReportedMax;

	public GAPExtension1(String prefix) {
		super(prefix);
		timeWindow = Configuration.getLong("delta_t");
		this.lastReportedMax = 0;
	}

	/**
	 * Send message between nodes via instantaneous transport
	 */
	protected void sendWithInstTransport(Node src, Node dest, Object event) {
		int pid = Configuration.getPid("ACTIVE_PROTOCOL");

		InstantaneousTransport t = (InstantaneousTransport) src
				.getProtocol(FastConfig.getTransport(pid));
		t.send(src, dest, event, pid);
	}

	/**
	 * set up a time out for a particular responseTime, use responseTime value
	 * as unique ID and time window as delay. This message is considered as
	 * internal message, thus won't be counted as overhead
	 * 
	 * @param pid
	 * @param element
	 */
	private void scheduleATimeOut(int pid, long element) {
		TimeOut timeOut = new TimeOut(element);
		Node dest = NodeUtils.getInstance().getNodeByID((long) this.myId);
		ConfigurableDelayTransport transport = ConfigurableDelayTransport
				.getInstance();
		transport.setDelay(this.timeWindow);
		transport.send(null, dest, timeOut, pid);
	}

	/**
	 * Send a message to my parent
	 * 
	 * @param node
	 * @param pid
	 */
	private void sendMsgToParent(Node node, int pid) {
		// TODO send msg to parent. If this is root node, send to all neighbors,
		// as a heart beat (actually, it's for initialization, DIRTY approach!
		if (this.parent == Double.POSITIVE_INFINITY)
			return;
		if (this.virgin == true && this.myId == 0) {
			sendMsgToAllNeighbor(node, pid);
			this.virgin = false;
		} else {
			Linkable linkable = (Linkable) node.getProtocol(FastConfig
					.getLinkable(pid));
			UpdateVector newMessage = composeMessage(node);
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
	 * Send a message to all neighbor
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
				Node peer = linkable.getNeighbor(i);
				// The selected peer could be inactive
				if (!peer.isUp())
					continue;
				InstantaneousTransport transport = new InstantaneousTransport();
				transport.send(node, peer, newMessage, pid);
			}
		}
	}

	/**
	 * Reassign subtree error budget based on my subtree table
	 * 
	 * @param node
	 * @param pid
	 */
	private void reassignErrBudget(Node node, int pid) {
		if (this.neighborList == null)
			return;
		Linkable linkable = (Linkable) node.getProtocol(FastConfig
				.getLinkable(pid));
		double errPerNode = errorBudgetOfSubtree / nodeNumInSubtree;
		for (int i = 0; i < linkable.degree(); ++i) {
			Node peer = linkable.getNeighbor(i);
			if (!peer.isUp() || peer.getID() == parent)
				continue;
			NodeStateVector state = this.neighborList
					.get((double) peer.getID());
			if (state == null)
				continue;
			if (state.status.equals("child")) {
				double errToAssign = errPerNode * ((double) state.nodeNum);
				ErrorBudget msg = new ErrorBudget(node, errToAssign);
				InstantaneousTransport transport = new InstantaneousTransport();
				transport.send(node, peer, msg, pid);
			}
		}
	}

	/**
	 * Test whether the change exceed error limit
	 * 
	 * @return
	 */
	private boolean testDiff() {
		if (Math.abs(lastReportedMax - this.maxReqTimeInSubtree) > errorBudgetOfSubtree) {
			lastReportedMax = maxReqTimeInSubtree;
			return true;
		} else {
			return false;
		}
	}

	// *********************************************************************
	// *********************************************************************

	@Override
	public void processEvent(Node node, int pid, Object event) {
		/*
		 * message type of new response time arrive, internal message
		 */
		if (event instanceof ResponseTimeArriveMessage) {
			final ResponseTimeArriveMessage newRequest = (ResponseTimeArriveMessage) event;
			long resTime = newRequest.getResponseTime();
			requestList.add(resTime);
			// System.out.println(requestList);
			scheduleATimeOut(pid, resTime);
			// System.out.print("Load change: " + this.value);
			computeLocalValue();
			computeSubtreeValue();
			if (testDiff()) {
				sendMsgToParent(node, pid);
			}

			/*
			 * Message type vector, external message
			 */
		} else if (event instanceof UpdateVector) {
			final UpdateVector msg = (UpdateVector) event;
			double oldLevel = this.level;
			double oldParent = this.parent;
			long oldNodeNum = this.nodeNumInSubtree;
			updateEntry(msg);
			findNewParent();
			computeSubtreeValue();
			if (this.level != oldLevel || this.parent != oldParent) {
				sendMsgToAllNeighbor(node, pid);
				return;
			}
			if (this.nodeNumInSubtree != oldNodeNum) {
				reassignErrBudget(node, pid);
			}
			if (testDiff()) {
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
			this.requestList.remove(msg.elementIndex);
			computeLocalValue();
			computeSubtreeValue();
			if (testDiff()) {
				sendMsgToParent(node, pid);
			}
		} else if (event instanceof ErrorBudget) {
			final ErrorBudget msg = (ErrorBudget) event;
			double oldErrorBudget = errorBudgetOfSubtree;
			errorBudgetOfSubtree = msg.errorBudget;
			if (errorBudgetOfSubtree != oldErrorBudget) {
				reassignErrBudget(node, pid);
			}
		}
	}

	@Override
	public void nextCycle(Node node, int protocolID) {
		// Implement your cycle-driven code for task 2 here

	}

	public GAPExtension1 clone() {
		return new GAPExtension1(prefix);

	}

}
