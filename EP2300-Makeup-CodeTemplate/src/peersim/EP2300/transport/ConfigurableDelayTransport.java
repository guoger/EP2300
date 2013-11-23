package peersim.EP2300.transport;

import peersim.config.*;
import peersim.core.*;
import peersim.edsim.*;
import peersim.transport.Transport;

/**
 * Transport class to send event to other servers a specify delay. Please use
 * this class only for sending to a local node.
 * 
 * @author Rerngvit Yanggratoke (rerngvit@kth.se)
 * 
 */
public final class ConfigurableDelayTransport implements Transport {

	// ---------------------------------------------------------------------
	// Fields
	// ---------------------------------------------------------------------
	private static final String PAR_DELAY = "delay";
	private long delay = 0;
	private static ConfigurableDelayTransport instance;

	// ---------------------------------------------------------------------
	// Initialization
	// ---------------------------------------------------------------------

	/**
	 * Reads configuration parameter.
	 */
	public ConfigurableDelayTransport(String prefix) {
		delay = Configuration.getLong(prefix + "." + PAR_DELAY);

	}

	public ConfigurableDelayTransport() {

	}

	public static ConfigurableDelayTransport getInstance() {
		if (instance == null)
			instance = new ConfigurableDelayTransport();

		return instance;

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
		// avoid calling nextLong if possible
		EDSimulator.add(delay, msg, dest, pid);
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

	public long getLatency(Node src, Node dest) {

		return delay;
	}

}
