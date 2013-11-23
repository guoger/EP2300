package peersim.EP2300.tasks;

import peersim.EP2300.base.GAPProtocolBase;
import peersim.cdsim.CDProtocol;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

public class GAPExtension1 extends GAPProtocolBase implements EDProtocol, CDProtocol{

	
	public GAPExtension1(String prefix) {
		super(prefix);
	}

	@Override
	public void processEvent(Node node, int pid, Object event) {
		// Implement your event-driven code for task 2 here
		
	}
	
	@Override
	public void nextCycle(Node node, int protocolID) {
		// Implement your cycle-driven code for task 2 here
		
	}
	
	
	public GAPExtension1 clone() {
		return new GAPExtension1(prefix);

	}
	
	
	

}
