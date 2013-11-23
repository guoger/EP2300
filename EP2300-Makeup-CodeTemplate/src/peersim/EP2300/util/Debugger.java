package peersim.EP2300.util;

import peersim.config.Configuration;
import peersim.core.CommonState;

public class Debugger {

	
	public static void debug(String text)
	{
		if (Configuration.getBoolean("debug_mode"))
			System.out.println(text);
	}

}
