package peersim.EP2300.control;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import peersim.EP2300.base.ResponseTimeTrace;
import peersim.EP2300.message.ResponseTimeArriveMessage;
import peersim.EP2300.transport.ConfigurableDelayTransport;
import peersim.EP2300.util.Debugger;
import peersim.EP2300.util.NodeUtils;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.edsim.ControlEvent;
import peersim.edsim.EDSimulator;

/**
 * Class for loading traces iteratively in order to not use up all memory of the
 * host machines.
 * 
 * @author Rerngvit Yanggratoke (rerngvit@kth.se)
 * 
 */
public class IterativeLoadGeneratorCtl extends ControlEvent {

	private List<ResponseTimeTrace> traces;
	private FileReader fileReader;
	private BufferedReader bufferedReader;

	public IterativeLoadGeneratorCtl() {
		traces = LoadGenerator.getTraces();
		String traceFilePath = Configuration.getString("trace_file");

		try {
			fileReader = new FileReader(traceFilePath);
			bufferedReader = new BufferedReader(fileReader);

			// read pass header
			bufferedReader.readLine();

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

	private ResponseTimeTrace getResponseTimeTrace(String raw) {
		String[] traceRaw = raw.split(",");
		ResponseTimeTrace trace = new ResponseTimeTrace(
				Long.valueOf(traceRaw[0].trim()), Integer.valueOf(traceRaw[1]
						.trim()), Integer.valueOf(traceRaw[2].trim()));
		return trace;
	}

	private int fetchTraces() {
		int fetchCount = 0;
		try {

			String line = null;
			// fetch trace for a number of times
			while ((line = bufferedReader.readLine()) != null) {

				ResponseTimeTrace trace = getResponseTimeTrace(line);
				traces.add(trace);

				fetchCount++;

				if (fetchCount > Configuration.getInt("NUM_TRACES_LOAD"))
					// break for now and fetch later for saving memory
					break;

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fetchCount;

	}

	private void executeTraces(int firstIndexThisRound) {
		int pid = Configuration.getPid("ACTIVE_PROTOCOL");

		for (int i = firstIndexThisRound; i < traces.size(); i++) {

			ResponseTimeTrace trace = traces.get(i);

			ResponseTimeArriveMessage arriveMsg = new ResponseTimeArriveMessage(
					trace.getResponseTime());
			Node dest = NodeUtils.getInstance().getNodeByID(trace.getNodeId());

			ConfigurableDelayTransport transport = ConfigurableDelayTransport
					.getInstance();
			transport.setDelay(trace.getTimeStamp() - CommonState.getTime());
			transport.send(null, dest, arriveMsg, pid);

		}

	}

	private void setTimerExecuteNext() {
		long nextExecutionTimeStamp = (long) (CommonState.getTime() + Configuration
				.getInt("LOAD_TRACES_EVERY"));

		EDSimulator.addControlEvent(nextExecutionTimeStamp, (byte) 0, this);
	}

	@Override
	public boolean execute() {

		// only load trace in the beginning or when it is needed
		if (CommonState.getTime() == 0
				|| (traces.size() > 0 && traces.get(traces.size() - 1).getTimeStamp()
						- CommonState.getTime() < 2 * Configuration
						.getInt("LOAD_TRACES_EVERY"))) {
			int firstIndexThisRound = traces.size();
			// 1. fetch traces
			int fetchCount = fetchTraces();

			if (fetchCount > 0)
			{
				// 2. execute newly fetch traces
				executeTraces(firstIndexThisRound);
	
				Debugger.debug("At time " + CommonState.getTime()
						+ " - Next set of traces from "
						+ traces.get(firstIndexThisRound).getTimeStamp() + " to "
						+ traces.get(traces.size() - 1).getTimeStamp()
						+ " are loaded");
			}
		}
		// 3. set Timer for future for executing traces again
		setTimerExecuteNext();

		return false;
	}

}
