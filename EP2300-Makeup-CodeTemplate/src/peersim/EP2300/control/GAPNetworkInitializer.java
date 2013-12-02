package peersim.EP2300.control;

import peersim.EP2300.vector.GAPNodeMax;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;

public class GAPNetworkInitializer implements Control {

	private final int pid;
	protected static final String ERROR_OBJECTIVE = "error_objective";

	private static final String PAR_PROT = "protocol";

	final private double errorObj;

	public GAPNetworkInitializer(String prefix) {
		// System.err.println(prefix);
		pid = Configuration.getPid(prefix + "." + PAR_PROT);
		this.errorObj = Configuration.getDouble(prefix + "." + ERROR_OBJECTIVE,
				800.0);
	}

	@Override
	public boolean execute() {
		for (int i = 0; i < Network.size(); i++) {
			GAPNodeMax prot = (GAPNodeMax) Network.get(i).getProtocol(pid);
			double id = Network.get(i).getID();
			prot.setInit(id, errorObj);

		}
		return false;
	}

}
