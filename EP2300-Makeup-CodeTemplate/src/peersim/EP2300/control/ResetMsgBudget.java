package peersim.EP2300.control;

import java.util.ArrayList;

import peersim.EP2300.tasks.GAPServerWithRateLimit;
import peersim.EP2300.vector.GAPNodeMax;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

public class ResetMsgBudget implements Control {

	private static final String PAR_PROT = "protocol";

	private final int protocolID;

	public ResetMsgBudget(String prefix) {
		protocolID = Configuration.getPid(prefix + "." + PAR_PROT);
	}

	@Override
	public boolean execute() {
		boolean orphan = false;
		GAPNodeMax rootNode;
		ArrayList orphanList = new ArrayList();
		for (int i = 0; i < Network.size(); ++i) {
			Node node = Network.get(i);
			//@formatter:off
			/*
			if (node.getID() == 0) {
				rootNode = ((GAPServerWithRateLimit) ((Node) node)
						.getProtocol(protocolID));
				System.err.println("Active requests: "
						+ rootNode.totalReqNumInSubtree);
				System.err.println("Active requests sum: "
						+ rootNode.totalReqTimeInSubtree);
			}
			*/
			//@formatter:on
			((GAPServerWithRateLimit) node.getProtocol(protocolID))
					.resetMsgBudget();
		}

		return false;
	}
}
