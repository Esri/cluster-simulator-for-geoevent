package com.esri.geoevent.clusterSimulator;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer implements Runnable
{
	private Simulator simulator;
	private Thread listener;
	private ServerSocket server;
	private volatile boolean running = true;

	public TCPServer(Simulator simulator)
	{
		this.simulator = simulator;
		try
		{
			server = new ServerSocket(5565);
		} catch (IOException e)
		{
			System.err.println("Could not start the TCP Server : " + e.getMessage() );
			return;
		}
		listener = new Thread(this);
		listener.start();
	}
	
	public void run()
	{
		while( running )
		{
			try {
				Socket socket = server.accept();
				ServerConnection conn = new ServerConnection( socket, simulator );
			} catch (IOException e) {
				System.err.println("Error while opening client connection : " + e.getMessage() );
			}
		}
	}

}
