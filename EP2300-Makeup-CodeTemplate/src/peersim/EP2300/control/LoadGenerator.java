package peersim.EP2300.control;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import peersim.EP2300.base.ResponseTimeTrace;
import peersim.EP2300.message.ResponseTimeArriveMessage;
import peersim.EP2300.transport.ConfigurableDelayTransport;
import peersim.EP2300.util.FileIO;
import peersim.EP2300.util.NodeUtils;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Node;
import peersim.edsim.EDSimulator;

/**
 * Load generator class for igniting the IterativeLoadGeneratorCtrl
 * 
 * @author Rerngvit Yanggratoke (rerngvit@kth.se)
 * 
 */
public class LoadGenerator implements Control {

	private static List<ResponseTimeTrace> traces;

	public static List<ResponseTimeTrace> getTraces() {
		return traces;
	}

	private void initResponseTimeTrace() {
		traces = new ArrayList<ResponseTimeTrace>();
		// read from trace files
	}

	private String prefix;

	public LoadGenerator(String prefix) {
		this.prefix = prefix;
		initResponseTimeTrace();
	}

	@Override
	public boolean execute() {
		IterativeLoadGeneratorCtl iterativeLoadControlEvent = new IterativeLoadGeneratorCtl();
		EDSimulator.addControlEvent(0, 0, iterativeLoadControlEvent);

		return false;
	}

}
