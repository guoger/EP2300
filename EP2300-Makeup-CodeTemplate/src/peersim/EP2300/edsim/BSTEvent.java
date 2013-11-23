package peersim.EP2300.edsim;

import peersim.core.Node;
import peersim.edsim.PriorityQ.Event;

/**
 * Custom class to represent event for an
 * event-driven simulation.
 * @author Rerngvit Yanggratoke (rerngvit@kth.se)
 *
 */
public class BSTEvent extends Event implements Comparable<BSTEvent>{

	private long priority;

	
	
	public BSTEvent(long priority, long time, Object event, Node node, byte pid) {
		super();
		this.priority = priority;
		this.time = time;
		this.event = event;
		this.node = node;
		this.pid = pid;
		
	}


	public long getPriority() {
		return priority;
	}



	@Override
	public int compareTo(BSTEvent other) {
		
		if (this.priority < other.priority) return -1;
		else if (this.priority == other.priority) return 0;
		else return 1;
		
	}
	

}
