package peersim.EP2300.tasks;

import peersim.EP2300.base.GAPProtocolBase;
import peersim.cdsim.CDProtocol;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

public class GAPServerWithRateLimit extends GAPProtocolBase implements EDProtocol, CDProtocol{

	
	public GAPServerWithRateLimit(String prefix) {
		super(prefix);
	}

	@Override
	public void processEvent(Node node, int pid, Object event) {
		// Implement your event-driven code for task 1 here
	}
	
	@Override
	public void nextCycle(Node node, int protocolID) {
		// Implement your cycle-driven code for task 1 here
		
	}
	
	
	public GAPServerWithRateLimit clone() {
		return new GAPServerWithRateLimit(prefix);

	}
	
	
	

}
