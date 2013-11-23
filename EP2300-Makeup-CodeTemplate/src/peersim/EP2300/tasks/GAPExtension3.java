package peersim.EP2300.tasks;

import peersim.EP2300.base.GAPProtocolBase;
import peersim.cdsim.CDProtocol;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

public class GAPExtension3 extends GAPProtocolBase implements EDProtocol, CDProtocol{

	
	public GAPExtension3(String prefix) {
		super(prefix);
	}

	@Override
	public void processEvent(Node node, int pid, Object event) {
		// Implement your event-driven code for task 3, P2 (optional) here
	}
	
	@Override
	public void nextCycle(Node node, int protocolID) {
		// Implement your cycle-driven code for task 3, P2 (optional) here
		
	}
	
	
	public GAPExtension3 clone() {
		return new GAPExtension3(prefix);

	}
	
	
	

}
