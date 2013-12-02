package peersim.EP2300.control;

import peersim.EP2300.tasks.GAPExtension1;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

public class NetObserver implements Control {
	private static final String PAR_PROT = "protocol";
	private final int protocolID;

	public NetObserver(String prefix) {
		protocolID = Configuration.getPid(prefix + "." + PAR_PROT);
	}

	@Override
	public boolean execute() {
		long max = 0;
		for (int i = 0; i < Network.size(); i++) {
			Node node = Network.get(i);
			GAPExtension1 prot = (GAPExtension1) node.getProtocol(protocolID);
			for (int j = 0; j < prot.requestList.size(); j++) {
				if (prot.requestList.get(j) > max)
					max = prot.requestList.get(j);
			}
		}
		System.out.println("Actual max is: " + max);

		return false;
	}

}
