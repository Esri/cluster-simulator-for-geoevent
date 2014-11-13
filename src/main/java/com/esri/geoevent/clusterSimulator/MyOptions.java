package com.esri.geoevent.clusterSimulator;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

public class MyOptions {

	public static Options createOptions() 
	{
		Options options = new Options();
		
		options.addOption( "m", "mode", true, "CLIENT|SERVER (default is CLIENT)");
		options.addOption( "h", "host", true, "The hostname of one machine in the cluster");
		options.addOption( "u", "username", true, "The user name of the primary site administrator for your ArcGIS Server.");
		options.addOption( "p", "password", true, "The password of the primary site administrator for your ArcGIS Server.");
		options.addOption( "f", "filename", true, "The file containing your simulation data.");
		options.addOption( "d", "delay", true, "The delay (in milliseconds) between simulated messages.");
		options.addOption( "b", "batch", true, "Then number of events to send in a batch (between pauses).  The default is 1.");
		options.addOption( "l", "loop", false, "Start the simulation again at the beginning when the end of the file is reached.");
		options.addOption( "t", "trustAllCerts", false, "Trust all SSL certificates.");
		
		return options;
	}

	public static void printUsageAndExit()
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( "GeoeventClusterSimulator", createOptions() );
		System.exit(1);
	}

}
