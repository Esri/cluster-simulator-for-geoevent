/*
  Copyright 1995-2014 Esri

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

  For additional information, contact:
  Environmental Systems Research Institute, Inc.
  Attn: Contracts Dept
  380 New York Street
  Redlands, California, USA 92373

  email: contracts@esri.com
 */
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
