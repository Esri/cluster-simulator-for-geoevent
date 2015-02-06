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

import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

public class FixedTimer implements SimulationTimer {

	private volatile Observer observer;
	private volatile Timer timer;
	private int batchSize;
	private int period;

	public FixedTimer(int batchSize, int period ) {
		this.batchSize = batchSize;
		this.period = period;
	}

	@Override
	public void start() 
	{
		timer = new Timer();
		timer.scheduleAtFixedRate( new Task(), 0, period );
	}

	@Override
	public void cancel()
	{
		if( timer != null )
		{
			timer.cancel();
			observer = null;
			timer = null;
		}
	}

	@Override
	public void setTimerObserver(Observer observer) {
		this.observer = observer;
	}

	private class Task extends TimerTask
	{
		public void run()
		{
			for( int i = 0; i < batchSize; i++ )
			{
				Observer obs = observer;
				if( obs != null )
					obs.update( null, this);
			}
		}
	}
}
