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

import com.esri.geoevent.clusterSimulator.simulator.Simulator;

public class Machine
{
	private String name;
	private String realTimeState;
	private Simulator simulator;
	private ClientConnection connection;
	
	public Machine( String name, String realTimeState, Simulator simulator )
	{
		this.name = name;
		this.realTimeState = realTimeState;
		this.simulator = simulator;
		if( realTimeState.equals("STARTED") )
			connect();
	}

	public String getRealTimeState() {
		return realTimeState;
	}

	public void setRealTimeState(String realTimeState) 
	{
		if( !this.realTimeState.equals(realTimeState) )
		{
			if( realTimeState.equals("STARTED") )
				connect();
			else if( realTimeState.equals("STOPPED") )
				disconnect();
			this.realTimeState = realTimeState;
		}
	}
	
	public void disconnect()
	{
		if( connection != null )
			connection.disconnect();
		connection = null;
	}

	private void connect() 
	{
		try {
			if( connection == null )
				connection = new ClientConnection(name, simulator);
			connection.connect();
		} catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getName() {
		return name;
	}
	


}
