package peersim.EP2300.control;

import peersim.EP2300.vector.GAPNode;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;

public class GAPNetworkInitializer implements Control {

	private final int pid;

	private static final String PAR_PROT = "protocol";

	public GAPNetworkInitializer(String prefix) {
		pid = Configuration.getPid(prefix + "." + PAR_PROT);
	}

	@Override
	public boolean execute() {
		for (int i = 0; i < Network.size(); i++) {
			GAPNode prot = (GAPNode) Network.get(i).getProtocol(pid);
			double id = Network.get(i).getID();
			prot.setInit(id);
		}
		return false;
	}

}
