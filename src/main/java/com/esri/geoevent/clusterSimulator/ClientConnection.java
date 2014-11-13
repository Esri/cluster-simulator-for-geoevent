package com.esri.geoevent.clusterSimulator;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

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
		simulator.addListener(this);
	}
	
	public void disconnect()
	{
		out = null;
		simulator.removeListener(this);
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
			System.out.println( machine + " : " + message);
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
