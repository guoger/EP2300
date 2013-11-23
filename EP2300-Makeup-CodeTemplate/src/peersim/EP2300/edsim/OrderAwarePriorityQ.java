package peersim.EP2300.edsim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import peersim.EP2300.edsim.algor.BinarySearchST;
import peersim.core.Node;
import peersim.edsim.PriorityQ;

/**
 * Class for insertion-order-aware priority queue, i.e., event that is inserted
 * at thte same time stamp will be executed in order of insertion. This is
 * required to support GAP implementation.
 * 
 * @author Rerngvit Yanggratoke
 * 
 */
public class OrderAwarePriorityQ implements PriorityQ {

	private final long DEFAULT_PRIORITY = 100;

	/**
	 * Use binary search tree to store event for efficient look up
	 */
	private BinarySearchST<Long, List<BSTEvent>> bsTree;

	public OrderAwarePriorityQ(String prefix) {
		bsTree = new BinarySearchST<Long, List<BSTEvent>>(10);
	}

	@Override
	public int size() {
		return bsTree.size();
	}

	@Override
	public void add(long time, Object event, Node node, byte pid) {
		List<BSTEvent> events = bsTree.get(time);
		long priority = DEFAULT_PRIORITY;
		if (events != null)
			priority = events.get(events.size() - 1).getPriority() + 1;
		else {
			events = new ArrayList<BSTEvent>();
			bsTree.put(time, events);
		}
		BSTEvent bstEvent = new BSTEvent(priority, time, event, node, pid);
		events.add(bstEvent);

	}

	@Override
	public void add(long time, Object event, Node node, byte pid, long priority) {
		BSTEvent bstEvent = new BSTEvent(priority, time, event, node, pid);
		List<BSTEvent> events = bsTree.get(time);
		if (events == null) {
			events = new ArrayList<BSTEvent>();
			bsTree.put(time, events);
		}
		events.add(bstEvent);
		Collections.sort(events);

	}

	@Override
	public Event removeFirst() {
		Long minTime = bsTree.min();
		List<BSTEvent> events = bsTree.get(minTime);

		if (events != null) {
			BSTEvent event = events.remove(0);

			if (events.size() == 0)
				bsTree.deleteMin();

			return event;
		} else {
			return null;
		}
	}

	@Override
	public long maxTime() {

		return Long.MAX_VALUE;
	}

	@Override
	public long maxPriority() {
		return 10000;
	}

}
