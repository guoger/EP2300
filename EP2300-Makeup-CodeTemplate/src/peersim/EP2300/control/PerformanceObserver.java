package peersim.EP2300.control;

import java.io.File;
import java.util.List;

import peersim.EP2300.base.GAPProtocolBase;
import peersim.EP2300.base.MessageTraceStats;
import peersim.EP2300.base.ResponseTimeTrace;
import peersim.EP2300.util.FileIO;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

/**
 * PerformanceObserver measures and reports performance metrics on the console
 * as well as on the output simulation file.
 * 
 * @author Rerngvit Yanggratoke (rerngvit@kth.se)
 * 
 */
public class PerformanceObserver implements Control {

	/**
	 * Time window of the measurements (delta_t)
	 */
	private long timeWindow = -1;

	public static void incrementMessageSizeCount(Node src, Node dest,
			long msgSize) {
		MessageTraceStats.getInstance().writeMessageTrace(src, dest, msgSize);
	}

	private String outResultFile;
	private String outputFolder;

	private long actualMax;
	private long estimatedMax;
	private long estimationMaxError;

	private double actualAverage;
	private double estimatedAverage;
	private double estimationAverageError;

	private double maxMessageRate;
	private double overhead;

	private List<ResponseTimeTrace> traces;

	/**
	 * Standard constructor that reads the configuration parameters. Invoked by
	 * the simulation engine.
	 * 
	 * @param prefix
	 *            the configuration prefix identifier for this class.
	 */
	public PerformanceObserver(String prefix) {
		traces = LoadGenerator.getTraces();
		timeWindow = Configuration.getLong("delta_t");
		assert (timeWindow != -1);

		outputFolder = Configuration.getString("sim_out_folder");
		File outputFolderFile = new File(outputFolder);
		if (!outputFolderFile.exists())
			outputFolderFile.mkdir();

		outResultFile = outputFolder + "/result.log";
		FileIO.delete(outResultFile);

	}

	private void getActualMeasurementsFromTrace() {
		long endTime = CommonState.getTime();
		long startTime = Math.max(0, endTime - timeWindow);

		long maxResponseTime = 0;
		double sumResponseTimes = 0;
		double numResponseTimes = 0;

		for (ResponseTimeTrace trace : traces) {
			// measure only within the time window
			if (trace.getTimeStamp() >= startTime
					&& trace.getTimeStamp() < endTime) {
				if (trace.getResponseTime() > maxResponseTime)
					maxResponseTime = trace.getResponseTime();
				sumResponseTimes += trace.getResponseTime();
				numResponseTimes++;
			}

			// break after passing the endTime
			if (trace.getTimeStamp() >= endTime)
				break;
		}

		actualMax = maxResponseTime;
		System.err.println("Actual sum: " + sumResponseTimes);
		System.err.println("Actual number of requests: " + numResponseTimes);
		actualAverage = sumResponseTimes / numResponseTimes;

		// remove unused traces that are before start time
		while (!traces.isEmpty()) {
			ResponseTimeTrace trace = traces.get(0);
			if (trace.getTimeStamp() < startTime)
				traces.remove(0);
			else
				break;
		}

	}

	private void measureRequests() {
		getActualMeasurementsFromTrace();

		// Find Node ID 0
		Node rootNode = null;
		for (int i = 0; i < Network.size(); i++) {
			Node node = Network.get(i);

			if (node.getID() == 0)
				rootNode = node;
		}
		assert (rootNode != null);

		GAPProtocolBase rootProtocol = (GAPProtocolBase) rootNode
				.getProtocol(Configuration.getPid("ACTIVE_PROTOCOL"));
		estimatedMax = rootProtocol.getEstimatedMax();
		estimationMaxError = Math.abs(actualMax - estimatedMax);

		estimatedAverage = rootProtocol.getEstimatedAverage();
		estimationAverageError = Math.abs(actualAverage - estimatedAverage);

	}

	private double getOverheadMaxMessageRate() {
		long maxCount = MessageTraceStats.getInstance().getMaxStat();

		double measuredTimesInSecond = getOverheadMeasuredTimeInSeconds();

		// max message rate
		double maxMessageRate = maxCount / measuredTimesInSecond;

		return maxMessageRate;

	}

	private double getOverheadMeasuredTimeInSeconds() {
		double measuredTime = CommonState.getTime() > timeWindow ? timeWindow
				: CommonState.getTime();
		return measuredTime / Configuration.getDouble("TIMES_ONE_SECOND");
	}

	// in messages/sec for all links
	private double getOverhead() {
		long sum = MessageTraceStats.getInstance().getSumStat();
		long numLinks = MessageTraceStats.getInstance().getNumLinks();
		double measuredTimesInSecond = getOverheadMeasuredTimeInSeconds();

		// compute average of #messages from all links
		double overhead = (sum) / (measuredTimesInSecond * numLinks);
		return overhead;
	}

	public void measureOverhead() {

		// compute stats
		MessageTraceStats.getInstance().calculateStats();
		maxMessageRate = getOverheadMaxMessageRate();
		overhead = getOverhead();

	}

	private void printOutput() {
		String output = String
				.format("At sec %.0f - f = %d, est_f = %d, err_f = %d, g = %.3f, est_g = %.3f, err_g = %.3f, r = %.3f, o = %.3f",
						CommonState.getTime()
								/ (double) Configuration
										.getInt("TIMES_ONE_SECOND"), actualMax,
						estimatedMax, estimationMaxError, actualAverage,
						estimatedAverage, estimationAverageError,
						maxMessageRate, overhead);

		System.out.println(output);

		String writeOutResult = String.format(
				"%.0f %d %d %d %.3f %.3f %.3f %.3f %.3f\n",
				CommonState.getTime()
						/ (double) Configuration.getInt("TIMES_ONE_SECOND"),
				actualMax, estimatedMax, estimationMaxError, actualAverage,
				estimatedAverage, estimationAverageError, maxMessageRate,
				overhead);

		FileIO.append(writeOutResult, outResultFile);

	}

	public boolean execute() {
		if (CommonState.getTime() > 0) {
			measureRequests();
			measureOverhead();
			printOutput();
		}
		return false;
	}

}
