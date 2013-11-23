package peersim.EP2300.base;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;

/**
 * A singleton helper class to compute link statistics between nodes. This class
 * will be used by the MessageTraceStats
 * 
 * @author Rerngvit Yanggratoke (rerngvit@kth.se)
 * 
 */
public class NodeLinkStats {
	private Map<NodeLink, Long> msgSizeMap;

	public NodeLinkStats() {
		msgSizeMap = new HashMap<NodeLink, Long>();

		// add all links
		for (int i = 0; i < Network.size(); i++) {
			Node node = Network.get(i);
			int linkableID = FastConfig.getLinkable(Configuration
					.getPid("ACTIVE_PROTOCOL"));
			Linkable linkable = (Linkable) node.getProtocol(linkableID);
			for (int j = 0; j < linkable.degree(); j++) {
				Node neighbor = linkable.getNeighbor(j);
				putNodeLinkStat(node, neighbor, 0l);
			}
		}
		if (Configuration.getBoolean("debug_mode"))
			debugLinkStats();
	}

	public void debugLinkStats() {
		for (NodeLink link : msgSizeMap.keySet()) {
			System.out.println("Link (" + link.getNode1().getID() + ","
					+ link.getNode2().getID() + ") => " + msgSizeMap.get(link));

		}

	}

	public Long getNodeLinkStat(Node node1, Node node2) {
		NodeLink link = new NodeLink(node1, node2);
		return msgSizeMap.get(link);
	}

	public void putNodeLinkStat(Node node1, Node node2, Long value) {
		NodeLink link = new NodeLink(node1, node2);
		msgSizeMap.put(link, value);

	}

	public void clearNodeLinkStat() {
		Iterator<Entry<NodeLink, Long>> it = msgSizeMap.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<NodeLink, Long> pairs = it.next();	        
	        pairs.setValue(0l);
	    }

	}

	public long getNumLinks() {
		return msgSizeMap.size();
	}

	public Long getMaxStat() {
		Long max = 0l;
		for (Long val : msgSizeMap.values()) {
			if (val > max)
				max = val;
		}
		return max;

	}

	public Long getSumStat() {
		Long sum = 0l;
		for (Long val : msgSizeMap.values()) {
			sum += val;
		}
		return sum;

	}

	public void incrementNodeLinkStat(Node node1, Node node2, Long incValue) {
		NodeLink link = new NodeLink(node1, node2);

		if (!isNodeLinkExists(node1, node2))
			putNodeLinkStat(node1, node2, 0l);

		Long curValue = msgSizeMap.get(link);
		msgSizeMap.put(link, curValue + incValue);

	}

	public boolean isNodeLinkExists(Node node1, Node node2) {
		NodeLink link = new NodeLink(node1, node2);

		return msgSizeMap.containsKey(link);

	}

}
