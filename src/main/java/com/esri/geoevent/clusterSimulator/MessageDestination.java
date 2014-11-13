package com.esri.geoevent.clusterSimulator;

import java.io.IOException;

public interface MessageDestination
{
	public void send(String message) throws IOException;
}
