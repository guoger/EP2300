package peersim.EP2300.message;

/**
 * A time out message (internal) to notify a node that certain entry is expired,
 * should reset the aggregate value
 * 
 * @author J
 * 
 */
public class TimeOut {

	final public long element;

	public TimeOut(long element) {
		this.element = element;
	}
}
