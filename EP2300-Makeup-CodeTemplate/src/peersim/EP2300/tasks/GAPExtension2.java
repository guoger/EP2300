package peersim.EP2300.tasks;

import peersim.EP2300.base.GAPProtocolBase;
import peersim.cdsim.CDProtocol;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

public class GAPExtension2 extends GAPProtocolBase implements EDProtocol, CDProtocol{

	
	public GAPExtension2(String prefix) {
		super(prefix);
	}

	@Override
	public void processEvent(Node node, int pid, Object event) {
		// Implement your event-driven code for task 3, P1 (optional) here
	}
	
	@Override
	public void nextCycle(Node node, int protocolID) {
		// Implement your cycle-driven code for task 3, P1 (optional) here
		
	}
	
	
	public GAPExtension2 clone() {
		return new GAPExtension2(prefix);

	}
	
	
	

}
