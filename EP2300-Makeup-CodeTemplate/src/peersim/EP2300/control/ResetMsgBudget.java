package peersim.EP2300.control;

import peersim.EP2300.tasks.GAPServerWithRateLimit;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;

public class ResetMsgBudget implements Control {

	private static final String PAR_PROT = "protocol";

	private final int protocolID;

	public ResetMsgBudget(String prefix) {
		protocolID = Configuration.getPid(prefix + "." + PAR_PROT);
	}

	@Override
	public boolean execute() {
		for (int i = 0; i < Network.size(); ++i) {
			((GAPServerWithRateLimit) Network.get(i).getProtocol(protocolID))
					.resetMsgBudget();
		}
		return false;
	}
}
