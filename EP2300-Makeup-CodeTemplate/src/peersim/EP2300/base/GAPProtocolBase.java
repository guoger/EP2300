package peersim.EP2300.base;

import peersim.EP2300.transport.InstantaneousTransport;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Node;

/**
 * Base class for the GAP protocol implementation. This class provides local
 * fields for reporting estimated values including estimatedMax and
 * estimatedAverage
 * 
 * @author Rerngvit Yanggratoke (rerngvit@kth.se)
 * 
 */
public class GAPProtocolBase {

	/**
	 * TODO Estimated value of f(t). Write your estimation here for the root
	 * node.
	 */
	public long estimatedMax;

	public long getEstimatedMax() {
		return estimatedMax;
	}

	/**
	 * TODO Should be override by child
	 */
	protected double estimatedAverage;

	public double getEstimatedAverage() {
		return estimatedAverage;
	}

	protected String prefix;

	public GAPProtocolBase(String prefix) {
		this.prefix = prefix;
	}

	protected void sendWithInstTransport(Node src, Node dest, Object event) {
		int pid = Configuration.getPid("ACTIVE_PROTOCOL");

		InstantaneousTransport t = (InstantaneousTransport) src
				.getProtocol(FastConfig.getTransport(pid));
		t.send(src, dest, event, pid);
	}

	public GAPProtocolBase clone() {
		return new GAPProtocolBase(prefix);

	}

}
