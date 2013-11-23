package peersim.EP2300.base;

import java.util.ArrayList;
import java.util.List;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;

/*
 * A Singleton and helper class for computing overhead. This will be used by the PerformanceObserver.
 * @author Rerngvit Yanggratoke (rerngvit@kth.se)
 */
public class MessageTraceStats {
    private static MessageTraceStats instance;
    private List<MessageTrace> msgTraces;

    private long timeWindow;
    private NodeLinkStats stats;
	
    public static MessageTraceStats getInstance()
    {
	if (instance == null)
	    instance = new MessageTraceStats();
	return instance;
		
    }
    public MessageTraceStats()
    {
	msgTraces = new ArrayList<MessageTrace>();
	timeWindow = Configuration.getLong("delta_t");
	stats      = new NodeLinkStats(); 
    }
	
    public void writeMessageTrace(Node src, Node dest, long msgSize)
    {
	NodeLink link = new NodeLink(src, dest);
	MessageTrace msgTrace = new MessageTrace(link, CommonState.getTime(), msgSize);
	msgTraces.add(msgTrace);
    }
	
	
	
    public void calculateStats()
    {
	stats.clearNodeLinkStat();

	long endTime = CommonState.getTime();
	long startTime = Math.max(0, endTime - timeWindow);


	for (MessageTrace trace : msgTraces) {
	    // measure only within the time window
	    if (trace.getTimeStamp() >= startTime && trace.getTimeStamp() < endTime) {
		stats.incrementNodeLinkStat(trace.getLink().getNode1(),
					    trace.getLink().getNode2(), trace.getMsgSize());
	    }

	    // break after passing the endTime
	    if (trace.getTimeStamp() >= endTime)
		break;
	}
		
	while (!msgTraces.isEmpty())
	    {
		MessageTrace trace = msgTraces.get(0);
		if (trace.getTimeStamp() < startTime)
		    msgTraces.remove(0);
		else break;
	    }
		
    }
	
    public Long getMaxStat() {
	return stats.getMaxStat();

    }

    public Long getSumStat() {
	return stats.getSumStat();
    }
    
    public Long getNumLinks() {
    	return stats.getNumLinks();
     }
	

}
