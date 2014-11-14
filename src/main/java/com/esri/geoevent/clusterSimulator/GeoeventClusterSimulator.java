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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.cert.X509Certificate;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class GeoeventClusterSimulator
{
	private static ServerAdminClient serverAdminClient;
	private static TCPServer tcpServer;
	
	static class CommandLineCertChecker implements CertificateChecker
	{

		@Override
		public boolean allowConnection(X509Certificate[] chain)
		{
			System.out.println("Trying to make https connection to site presenting following certificate:");
			System.out.println(chain[0]);
			System.out.print("Allow connection? (y|n): ");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			 
		    String response = null;
		 
		    //  read the username from the command-line; need to use try/catch with the
		    //  readLine() method
		    try
		    {
		         response = br.readLine();
		    }
		    catch (IOException ioe)
		    {
		         System.out.println("IO error trying to read your response.");
		         
		    }
			return response.charAt(0) == 'y';
		}
		
	}

	public static void main( String[] args )
	{
		Options options = MyOptions.createOptions();
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = null;
		try
		{
			cmd = parser.parse( options, args );
		} catch (ParseException e1) 
		{
			MyOptions.printUsageAndExit();
		}
		
		try 
		{
			ClientServerMode mode = getMode(cmd.getOptionValue('m',"CLIENT"));
			Simulator simulator = null;
			int batchSize = 1;
			if( cmd.hasOption('b') )
			{
				try
				{
					batchSize = Integer.parseInt(cmd.getOptionValue('b') );
					if( batchSize < 1 )
					{
						batchSize = 1;
						System.err.println("Error : " + cmd.getOptionValue('b') + " is not a valid value for the batch size.  Using \'1\' instead.");
					}
				}catch( NumberFormatException ex )
				{
					System.err.println("Error : " + cmd.getOptionValue('b') + " is not a valid value for the batch size.");
				}
			}
			boolean trustAllSSLCerts = cmd.hasOption('t');
			switch (mode) {
			case CLIENT:
				simulator = getSimulator(cmd.getOptionValue('f'), true, cmd.hasOption('l'), batchSize);
				serverAdminClient = new ServerAdminClient( cmd.getOptionValue('h'), cmd.getOptionValue('u',"siteadmin"), cmd.getOptionValue('p',"password"), simulator, (trustAllSSLCerts) ? new AcceptAlwaysCertChecker() :new CommandLineCertChecker() );
				simulator.setPeriod(Long.parseLong(cmd.getOptionValue('d', "1000") ));
				simulator.start();
				break;
			case SERVER:
				simulator = getSimulator(cmd.getOptionValue('f'), false, cmd.hasOption('l'), batchSize);
				tcpServer = new TCPServer(simulator);
				simulator.setPeriod(Long.parseLong(cmd.getOptionValue('d', "1000") ));
				simulator.start();
				break;
			default:
				break;
			}
		} 
		catch (Exception e) 
		{
			System.err.println("Error : " + e.getMessage() );
			System.exit(1);
		}
	}

	private static Simulator getSimulator(String filename, boolean roundRobinDataToClients, boolean loop, int batchSize ) throws FileNotFoundException 
	{
		if( filename == null )
			MyOptions.printUsageAndExit();
		File simulationFile = new File(filename);
		if( !simulationFile.exists() )
		{
			System.err.println("ERROR File Does not exist : " + filename);
			System.exit(1);
		}
		Simulator simulator = new Simulator( null );
		simulator.setRoundRobinDataToAllClients(roundRobinDataToClients);
		simulator.setSimulationFile(simulationFile);
		simulator.setLoop(loop);
		simulator.setBatchSize(batchSize);
		return simulator;
	}

	private static ClientServerMode getMode(String string)
	{
		ClientServerMode mode = ClientServerMode.valueOf(string.toUpperCase() );
		return mode;
	}

}