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

public class GAPExtension1 extends GAPNode implements EDProtocol, CDProtocol {

	protected static final String ERROR_OBJECTIVE = "error_objective";
	private final double errorBudget;
	private double lastReportedMax;
	private double lastReportedTotalResponseTime;
	private double lastReportedTotalNum;

	public GAPExtension1(String prefix) {
		super(prefix);
		double errorObj = (Configuration.getDouble(prefix + "."
				+ ERROR_OBJECTIVE, 5.0));
		this.errorBudget = errorObj;
		timeWindow = Configuration.getLong("delta_t");
		this.lastReportedMax = 0;
		this.lastReportedTotalNum = 0;
		this.lastReportedTotalResponseTime = 0;
	}

	protected void sendWithInstTransport(Node src, Node dest, Object event) {
		int pid = Configuration.getPid("ACTIVE_PROTOCOL");

		InstantaneousTransport t = (InstantaneousTransport) src
				.getProtocol(FastConfig.getTransport(pid));
		t.send(src, dest, event, pid);
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

	private boolean testDiff() {
		double lastReportedEst;
		if (this.lastReportedTotalNum == 0) {
			lastReportedEst = 0;
		} else {
			lastReportedEst = this.lastReportedTotalResponseTime
					/ this.lastReportedTotalNum;
		}
		if ((Math.abs(lastReportedMax - this.maxReqTimeInSubtree) > errorBudget)
				|| (Math.abs(lastReportedEst - this.estimatedAverage) > errorBudget)) {
			lastReportedTotalNum = totalReqNumInSubtree;
			lastReportedMax = maxReqTimeInSubtree;
			lastReportedTotalResponseTime = totalReqTimeInSubtree;
			return true;
		} else {
			return false;
		}
	}

//@formatter:off
	/*
	private boolean testDiff() {
		// N -> 0 or 0 -> N
		if ((lastReportedMax == 0 && totalReqTimeInSubtree != 0)
				|| (lastReportedMax != 0 && totalReqTimeInSubtree == 0)) {
			lastReportedMax = maxReqTimeInSubtree;
			lastReportedTotalResponseTime = totalReqTimeInSubtree;
			lastReportedTotalNum = totalReqNumInSubtree;
			return true;
		}
		if (lastReportedMax == 0 && totalReqTimeInSubtree == 0) {
			return false;
		}
		boolean maxResTimeExceedErrorObj = ((Math.abs(maxReqTimeInSubtree
				- lastReportedMax) / lastReportedMax) > errorObj);
		double lastReportedAvg = lastReportedTotalResponseTime
				/ lastReportedTotalNum;
		boolean avgResTimeExceedErrorObj = (Math.abs(lastReportedAvg
				- estimatedAverage)
				/ lastReportedAvg > errorObj);
		if (maxResTimeExceedErrorObj || avgResTimeExceedErrorObj) {
			lastReportedMax = maxReqTimeInSubtree;
			lastReportedTotalResponseTime = totalReqTimeInSubtree;
			lastReportedTotalNum = totalReqNumInSubtree;
			return true;
		} else {
			return false;
		}
	}
*/
//@formatter:on
	// *********************************************************************
	// *********************************************************************

	@Override
	public void processEvent(Node node, int pid, Object event) {
		// Implement your event-driven code for task 2 here
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
		} else if (event instanceof UpdateVector) {
			final UpdateVector msg = (UpdateVector) event;
			double oldLevel = this.level;
			updateEntry(msg);
			findNewParent();
			computeSubtreeValue();
			if (this.level != oldLevel) {
				sendMsgToAllNeighbor(node, pid);
				return;
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
