package com.esri.geoevent.clusterSimulator;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class ServerConnection implements MessageDestination
{

	private Simulator simulator;
	private OutputStream out;

	public ServerConnection(Socket socket, Simulator simulator) throws IOException 
	{
		out = socket.getOutputStream();
		this.simulator = simulator;
		simulator.addListener(this);
	}

	@Override
	public void send(String message) throws IOException 
	{
		System.out.println("Sending : " + message);
		byte[] bytes = (message+"\n").getBytes();
		try
		{
			out.write(bytes);
			out.flush();
		}catch(IOException ex)
		{
			simulator.removeListener(this);
			throw new IOException( ex.getMessage() );
		}
	}

}
