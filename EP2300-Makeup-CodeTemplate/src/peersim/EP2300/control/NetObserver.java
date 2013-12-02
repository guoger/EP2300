package peersim.EP2300.control;

import peersim.EP2300.tasks.GAPExtension1;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

public class NetObserver implements Control {
	private static final String PAR_PROT = "protocol";
	private final int protocolID;
	private final long errorBudget;

	public NetObserver(String prefix) {
		protocolID = Configuration.getPid(prefix + "." + PAR_PROT);
		errorBudget = Configuration.getLong("e");
	}

	@Override
	public boolean execute() {
		long max = 0;
		long report = 0;
		long estMax = 0;
		GAPExtension1 protMax = null;
		GAPExtension1 protEstMax = null;
		GAPExtension1 protReport = null;
		for (int i = 0; i < Network.size(); i++) {
			Node node = Network.get(i);
			GAPExtension1 prot = (GAPExtension1) node.getProtocol(protocolID);
			for (int j = 0; j < prot.requestList.size(); j++) {
				if (prot.requestList.get(j) > max) {
					max = prot.requestList.get(j);
					protMax = prot;
				}
			}
			if (prot.lastReportedMax > report) {
				report = prot.lastReportedMax;
				protReport = prot;
			}
			if (prot.estimatedMax > estMax) {
				estMax = prot.estimatedMax;
				protEstMax = prot;
			}
			// System.out.print(prot.myId + ": " + prot.lastReportedMax + "\t");
		}
		if (Math.abs(max - estMax) > errorBudget) {
			System.err.println("EEEEEEEEEEEEEEEEEError!");
			System.err.println("Max last reported is:" + report + " @ "
					+ protReport.myId);
			System.err.println("Estimated max is: " + estMax + " @ "
					+ protEstMax.myId);
			System.err.println("Actual max is @ " + protMax.myId);
			System.err.println("\tValue:\t\t" + max);
			System.err.println("\tLast reported:\t" + protMax.lastReportedMax);
			System.err.println("\tMax Req Time in subtree: "
					+ protMax.maxReqTimeInSubtree);
			System.err.println("\tMax Req Time local: "
					+ protMax.maxReqTimeLocal);
			System.err.println("\tLevel: \t" + protMax.level);
			System.err.println("\t" + protMax.requestList.toString());
		}
		return false;
	}

}
