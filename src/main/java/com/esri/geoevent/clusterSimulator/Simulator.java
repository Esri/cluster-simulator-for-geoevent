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
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import com.esri.geoevent.clusterSimulator.ui.Controller;

public class Simulator
{
	private File simulationFile;
	private BufferedReader in;
	private CopyOnWriteArrayList<MessageDestination> listeners = new CopyOnWriteArrayList<>();
	private Iterator<MessageDestination> iterator = null;
	private Timer timer;
	private boolean roundRobinDataToClients;
	private volatile boolean looping = false;
	private int batchSize;
	private long period;
	private long lastSend;
	private long sentEventCount;
	private long totalEventCount;
	private String filename;
	private Controller controller;

	public String getFilename() {
		return filename;
	}

	private class Task extends TimerTask
	{
		public void run()
		{
			for( int i = 0; i < batchSize; i++ )
			{
				try {
					sendMessage();
				} catch (EndOfFileException e) {
					cancel();
					timer = null;
					controller.endOfSimulationReached();
				}
			}
		}
	}

	public Simulator( Controller controller )
	{
		this.controller = controller;
	}

	public void setRoundRobinDataToAllClients( boolean roundRobinDataToClients )
	{
		this.roundRobinDataToClients = roundRobinDataToClients;
	}

	private long getFileLength( File file )
	{
		long count = 0;
		try(BufferedReader in = new BufferedReader( new FileReader( simulationFile ) ))
		{
			while( in.readLine() != null )
				count++;
			return count;
		}catch(IOException ex)
		{
			// do nothing, just return count.
		}
		return count;
	}

	public void setSimulationFile(File file) throws FileNotFoundException
	{
		this.simulationFile = file;
		in = new BufferedReader( new FileReader( simulationFile ) );
		totalEventCount = getFileLength( file );
		filename = file.getName();
	}

	public synchronized void start( ) throws IOException
	{
		if( in == null )
			throw new IOException( "No simulation file loaded." );
		timer = new Timer();
		timer.scheduleAtFixedRate( new Task(), 0, period );
	}

	public synchronized void stop()
	{
		timer.cancel();
		timer = null;
	}

	private synchronized void restartTimerWithNewPeriod()
	{
		if( timer != null )
		{
			timer.cancel();
			long timeSinceLastSend = System.currentTimeMillis() - lastSend;
			if( timeSinceLastSend > period )
				timeSinceLastSend = period;
			if( timeSinceLastSend < 0 )
				timeSinceLastSend = 0;
			long delay = period - timeSinceLastSend;
			timer = new Timer();
			timer.scheduleAtFixedRate( new Task(), delay, period );
		}
	}

	public synchronized void addListener( MessageDestination out )
	{
		listeners.add(out);
	}

	public synchronized void removeListener( MessageDestination out )
	{
		listeners.remove(out);
	}

	private void sendMessage() throws EndOfFileException
	{
		lastSend = System.currentTimeMillis();
		String line = null;
		try
		{
			line = in.readLine();
			if( line == null && looping  )
			{
				in = new BufferedReader( new FileReader( simulationFile ) );
				line = in.readLine();
			}
		}catch(IOException ex)
		{
			System.err.println("Error while reading from the simulation file. : " + ex.getMessage() );
			return;
		}
		
		if( line == null )
			throw new EndOfFileException( "End of file "+filename+" reached.");

		if( roundRobinDataToClients )
		{

			boolean sent = false;
			MessageDestination out = getNextDestination();
			MessageDestination firstDestination = out;
			while( !sent )
			{
				if( out == null )
					break;

				try
				{
					out.send(line);
					sent = true;
				}catch(IOException ex)
				{
					out = getNextDestination();
					if( out == firstDestination )
						break;
				}
			}
		}
		else
		{
			iterator = listeners.iterator();
			while(iterator.hasNext())
			{
				MessageDestination out = iterator.next();
				try {
					out.send(line);
				} catch (IOException e) 
				{
					System.err.println("Failed to send message.  Error : " + e.getMessage());
				}
			}
		}

		sentEventCount++;		
	}

	private MessageDestination getNextDestination()
	{
		if( iterator == null )
			iterator = listeners.iterator();

		if( !iterator.hasNext() )
		{
			if(listeners.size() == 0)
				return null;
			iterator = listeners.iterator();
			if( !iterator.hasNext() )
				return null;
		}

		return iterator.next();
	}

	public void setPeriod( long period )
	{
		this.period = period;
		restartTimerWithNewPeriod();
	}

	public void setLoop(boolean l)
	{
		looping = l;	
	}

	public void setBatchSize( int batchSize )
	{
		this.batchSize = batchSize;
	}

	public boolean isRunning() 
	{
		return ( timer != null );
	}

	public void step() throws EndOfFileException
	{
		sendMessage();
	}

	public boolean isReady() 
	{
		return simulationFile != null;
	}

	public void rewind()
	{
		if( simulationFile != null )
		{
			try
			{
				in = new BufferedReader( new FileReader( simulationFile ) );
				if( timer != null )
				{
					timer.cancel();
					timer = null;
				}
			}catch(FileNotFoundException ex )
			{
				System.err.println("Error reading the simulation file.");
			}
		}

	}

	public long getSentEventCount()
	{
		return sentEventCount;
	}

	public long getTotalEventCount() 
	{
		return totalEventCount;
	}

}
