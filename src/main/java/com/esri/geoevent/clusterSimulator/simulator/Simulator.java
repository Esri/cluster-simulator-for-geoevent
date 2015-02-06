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
package com.esri.geoevent.clusterSimulator.simulator;

import java.io.File;
import java.io.IOException;
import java.util.Observer;

import com.esri.geoevent.clusterSimulator.MessageDestination;

public interface Simulator
{
	public void start();
	public void stop();
	public void step();
	public void rewind();
	public void setSimulationFile(File simulationFile, int skipLines, int timeField ) throws IOException;
	public void setFixedRate(int messages, int period);
	public void setRealRate(float percent);
	public boolean isRunning();
	public boolean isReady();
	public void addMessageListener(MessageDestination listener);
	public void removeMessageListener(MessageDestination out);
	public void addEndOfSimulationListener(Observer listener);
	public long getSentEventCount();
	public long getTotalEventCount();
	public String getFilename();
	public void setLooping( boolean looping );
	public void resetCounter();
}
