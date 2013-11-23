package peersim.EP2300.base;

import peersim.core.Node;

/**
 * Class to represent link between nodes
 * @author Rerngvit Yanggratoke (rerngvit@kth.se)
 *
 */
public class NodeLink {

	private Node node1;
	private Node node2;
	
	public Node getNode1() {
		return node1;
	}
	public Node getNode2() {
		return node2;
	}
	public NodeLink(Node node1, Node node2) {
		super();
		this.node1 = node1;
		this.node2 = node2;
	}
	
	
	
	@Override
	public boolean equals(Object obj) {
	    if (obj == null) {
	        return false;
	    }
	    if (getClass() != obj.getClass()) {
	        return false;
	    }
	    final NodeLink other = (NodeLink) obj;
	    if ((this.node1 == null) ? (other.node1 != null) : !this.node1.equals(other.node1)) {
	        return false;
	    }
	    if ((this.node2 == null) ? (other.node2 != null) : !this.node2.equals(other.node2)) {
	        return false;
	    }
	    return true;
	}
	
	@Override
	public int hashCode() {
	    int hash = 3;
	    hash = 53 * hash + (this.node1 != null ? this.node1.hashCode() : 0);
	    hash = 53 * hash + (this.node2 != null ? this.node2.hashCode() : 0);
	    return hash;
	}


}
