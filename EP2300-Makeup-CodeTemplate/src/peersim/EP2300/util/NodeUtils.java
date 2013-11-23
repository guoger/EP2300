package peersim.EP2300.util;

import java.util.HashMap;
import java.util.Map;

import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;

/**
 * This utility class is for getting Node from ID
 * @author Rerngvit Yanggratoke (rerngvit@kth.se)
 *
 */
public class NodeUtils {

	private static NodeUtils instance;
	
	public static NodeUtils getInstance()
	{
		if (instance == null)
			instance = new NodeUtils();
		return instance;
	}
	
	Map<Long, Node> idNodeMaps;
	public NodeUtils()
	{
		idNodeMaps = new HashMap<Long, Node>();
		for (int i = 0; i < Network.size(); i++)
		{
			Node node = Network.get(i);
			idNodeMaps.put(node.getID(), node);
		}
		
		
	}
	
	public Node getNodeByID(long ID)
	{
		
		Node node =  idNodeMaps.get(ID);
		assert(node.getID() == ID);
		return node;
	}

}
