package peersim.EP2300.message;

import peersim.core.Node;

public class ErrorBudget {
	final public Node sender;
	final public double errorBudget;

	public ErrorBudget(Node sender, double errB) {
		this.sender = sender;
		this.errorBudget = errB;
	}
}
