package peersim.EP2300.transport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import peersim.EP2300.base.GAPProtocolBase;
import peersim.EP2300.control.PerformanceObserver;
import peersim.config.*;
import peersim.core.*;
import peersim.edsim.*;
import peersim.transport.Transport;
import se.kth.ees.lcn.ep2300.ObjectSizeFetcher;

/**
 * Transport class to send between nodes.
 * 
 * @author Rerngvit Yanggratoke (rerngvit@kth.se)
 * 
 */
public final class InstantaneousTransport implements Transport {
	private final long CONSTANT_LATENCY = 0;

	// ---------------------------------------------------------------------
	// Initialization
	// ---------------------------------------------------------------------

	/**
	 * Reads configuration parameter.
	 */
	public InstantaneousTransport(String prefix) {

	}

	public InstantaneousTransport() {

	}

	public Object clone() {
		return this;
	}

	// ---------------------------------------------------------------------
	// Methods
	// ---------------------------------------------------------------------

	/**
	 * Delivers the message with a random delay, that is drawn from the
	 * configured interval according to the uniform distribution.
	 */
	public void send(Node src, Node dest, Object msg, int pid) {
		EDSimulator.add(CONSTANT_LATENCY, msg, dest, pid);

		// measure event obj size in byte
		long msgSize = ObjectSizeFetcher.getObjectSize(msg);

		assert (msgSize <= Configuration.getLong("MSG_SIZE_LIMIT"));
		
		if (msgSize > Configuration.getLong("MSG_SIZE_LIMIT"))
		{
			System.err.println("Terminating: msgSize is "  + 
		msgSize + " bytes, which exceeds the limit of " + Configuration.getLong("MSG_SIZE_LIMIT") + " bytes");
			System.exit(1);
		}
		
		PerformanceObserver.incrementMessageSizeCount(src, dest, 1);
	}

	public long getLatency(Node src, Node dest) {
		return CONSTANT_LATENCY;
	}

}
