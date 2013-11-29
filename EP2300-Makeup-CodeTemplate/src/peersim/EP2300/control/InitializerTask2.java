package peersim.EP2300.control;

import peersim.EP2300.vector.GAPNodeTask2;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;

public class InitializerTask2 implements Control {

	final private int pid;
	protected static final String ERROR_OBJECTIVE = "error_objective";
	private static final String PAR_PROT = "protocol";
	final private double errorObj;

	public InitializerTask2(String prefix) {
		// System.err.println(prefix);
		pid = Configuration.getPid(prefix + "." + PAR_PROT);
		this.errorObj = Configuration.getDouble(prefix + "." + ERROR_OBJECTIVE,
				800.0);
	}

	@Override
	public boolean execute() {
		for (int i = 0; i < Network.size(); i++) {
			GAPNodeTask2 prot = (GAPNodeTask2) Network.get(i).getProtocol(pid);
			double id = Network.get(i).getID();
			prot.setInit(id, errorObj);
		}
		return false;
	}

}
