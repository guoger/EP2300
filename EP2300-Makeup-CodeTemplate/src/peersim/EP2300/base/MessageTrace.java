package peersim.EP2300.base;

/**
 * Class to represent a message trace for computing overhead by the PerformanceObserver
 * @author Rerngvit Yanggratoke (rerngvit@kth.se)
 */
public class MessageTrace {
    
	private NodeLink link;
	private long timeStamp;
	private long msgSize;
	
	public MessageTrace(NodeLink link, long timeStamp, long msgSize) {
		super();
		this.link = link;
		this.timeStamp = timeStamp;
		this.msgSize = msgSize;
	}
	public long getMsgSize() {
		return msgSize;
	}
	public NodeLink getLink() {
		return link;
	}
	public long getTimeStamp() {
		return timeStamp;
	}
	
}
