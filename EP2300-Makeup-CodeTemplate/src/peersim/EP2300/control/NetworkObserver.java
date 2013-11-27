package peersim.EP2300.control;

import java.util.ArrayList;

import peersim.EP2300.tasks.GAPServerWithRateLimit;
import peersim.EP2300.vector.GAPNode;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

public class NetworkObserver implements Control {

	private static final String PAR_PROT = "protocol";
	private final int protocolID;

	public NetworkObserver(String prefix) {
		protocolID = Configuration.getPid(prefix + "." + PAR_PROT);
	}

	@Override
	public boolean execute() {
		boolean orphan = false;
		int[] levelList = new int[10];
		double level;
		int nodeC = 0;
		GAPNode rootNode;
		GAPServerWithRateLimit p;
		ArrayList<Long> orphanList = new ArrayList<Long>();
		for (int i = 0; i < Network.size(); ++i) {
			Node node = Network.get(i);
			if (node.getID() == 0) {
				// Change here to adapt to different tasks
				rootNode = ((GAPServerWithRateLimit) ((Node) node)
						.getProtocol(protocolID));
				System.err.println("Active requests: "
						+ rootNode.totalReqNumInSubtree);
				System.err.println("Active requests sum: "
						+ rootNode.totalReqTimeInSubtree);
				System.err
						.println("Nodes number: " + rootNode.nodeNumInSubtree);
				System.err.println("Actual network size: " + Network.size());
				// rootNode.printNeighbor();
			}
			p = ((GAPServerWithRateLimit) node.getProtocol(protocolID));
			level = p.level;

			if (level == 1) {
				nodeC += ((GAPServerWithRateLimit) node.getProtocol(protocolID)).nodeNumInSubtree;
			}

			// Change here to adapt to different tasks
			if (Double.isInfinite((((GAPServerWithRateLimit) node
					.getProtocol(protocolID)).level))) {
				orphan = true;
				orphanList.add(Network.get(i).getID());
			} else {
				levelList[(int) level]++;
			}
		}
		if (orphan) {
			System.err.println(orphanList);
		}
		System.err.println("level 1 node count: " + nodeC);
		for (int i = 0; i < 10; i++)
			System.err.print(levelList[i] + "\t");
		System.err.println("\n");
		return false;
	}
}
