package peersim.EP2300.control;

import peersim.EP2300.tasks.GAPExtension3;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

public class ReassignErrorBudget implements Control {

	public ReassignErrorBudget(String prefix) {

	}

	@Override
	public boolean execute() {
		Node rootNode = null;
		for (int i = 0; i < Network.size(); i++) {
			Node node = Network.get(i);

			if (node.getID() == 0)
				rootNode = node;
		}

		int pid = Configuration.getPid("ACTIVE_PROTOCOL");
		GAPExtension3 rootProtocol = (GAPExtension3) rootNode.getProtocol(pid);
		rootProtocol.reassignErrorObj(rootNode, pid);
		return false;
	}
}
