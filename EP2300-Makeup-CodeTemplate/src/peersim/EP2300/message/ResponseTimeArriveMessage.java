package peersim.EP2300.message;

/**
 * Class to represent the event of response time arrives
 * at a server. Please use this to update local variable.
 *
 */
public class ResponseTimeArriveMessage {

	private long responseTime;

	public long getResponseTime() {
		return responseTime;
	}

	public ResponseTimeArriveMessage(long responseTime) {
		super();
		this.responseTime = responseTime;
	}
	

}
