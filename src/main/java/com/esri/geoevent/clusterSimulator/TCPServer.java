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
