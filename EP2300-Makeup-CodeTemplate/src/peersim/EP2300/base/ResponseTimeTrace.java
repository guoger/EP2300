package peersim.EP2300.base;

/**
 * Class to represent ResponseTime object.
 * This will be used to compute actual requests within a time window
 * @author Rerngvit Yanggratoke (rerngvit@kth.se)
 *
 */
public class ResponseTimeTrace {

	/**
	 *  Timestamp in msec
	 */
	private long timeStamp;

	public ResponseTimeTrace(long timeStamp, long nodeId, long responseTime) {
		super();
		this.timeStamp = timeStamp;
		this.nodeId = nodeId;
		this.responseTime = responseTime;
	}

	/**
	 * Node ID that this response time occurs at the said timestamp
	 */
	private long nodeId;

	/**
	 *  responseTimes in msec
	 */
	private long responseTime;

	public long getTimeStamp() {
		return timeStamp;
	}

	public long getNodeId() {
		return nodeId;
	}

	public long getResponseTime() {
		return responseTime;
	}

}
