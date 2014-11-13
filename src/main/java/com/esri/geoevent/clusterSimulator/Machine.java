package com.esri.geoevent.clusterSimulator;

import java.io.IOException;

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
