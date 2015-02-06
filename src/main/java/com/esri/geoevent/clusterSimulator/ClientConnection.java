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

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import com.esri.geoevent.clusterSimulator.simulator.Simulator;

public class ClientConnection implements MessageDestination
{
	private Socket socket;
	private OutputStream out;
	private String machine;
	private Simulator simulator;
	
	public ClientConnection(String machine, Simulator simulator) 
	{
		this.machine = machine;
		this.simulator = simulator;
	}
	
	public void connect() throws UnknownHostException, IOException
	{
		socket = new Socket( machine, 5565 );
		out = socket.getOutputStream();
		simulator.addMessageListener(this);
	}
	
	public void disconnect()
	{
		out = null;
		simulator.removeMessageListener(this);
		try {
			socket.close();
		} catch (IOException e) 
		{
			System.err.println("Error while closing socket to " + machine + " : " + e.getMessage() );
		}
	}

	@Override
	public void send(String message) throws IOException 
	{
		if( out == null )
			throw new IOException( "Not connected to " + machine );
		try {
			byte[] bytes = (message+"\n").getBytes();
			out.write(bytes);
			out.flush();
		} catch (IOException e) 
		{
			tryReconnect();
			throw new IOException( e.getMessage() );
		}
	}
	
	private void tryReconnect()
	{
		Thread t = new Thread( new ReconnectTask() );
		t.setDaemon(true);
		t.setName("Client connection reconnect thread for machine " + machine );
		t.start();
	}
	
	class ReconnectTask implements Runnable
	{
		public void run()
		{
			try {
				socket = new Socket( machine, 5565 );
				out = socket.getOutputStream();
			} catch (IOException e) 
			{
				//System.err.println("Error while trying to reconnect to " + machine + " : " + e.getMessage() );
			}
		}
	}
}
